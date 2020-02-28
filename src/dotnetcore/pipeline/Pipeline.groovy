package dotnetcore.pipeline;

import config.*;
import interfaces.pipeline.stage.GenericStage;
import dotnetcore.pipeline.stage.*;
import utils.script.ScriptAdapter;
import utils.jenkins.JenkinsAdapter;
import utils.vcs.VCSAdapter;
import utils.PipelineStages;
import versioning.interfaces.GenericVersioningSystem;

import java.lang.reflect.*;

import static utils.FileUtils.getLinuxFilesOnRegex;
import static utils.script.ScriptAdapter.log;

class Pipeline implements Serializable {
    boolean isArtifactRequested = false;
    Map csprojVersionMap = [:];
    PipelineConfig config;
    PrivateConfig privateConfig;
    Stack<GenericStage> executedStages = new Stack<GenericStage>();
    Map<String, GenericStage> customizedStageMap = [:];
    GenericVersioningSystem versioningSystem = null;

    Pipeline(PipelineConfig config, PrivateConfig privateConfig) {
        this.config = config;
        this.privateConfig = privateConfig;

        JenkinsAdapter.binaryRepoUser = privateConfig.binaryRepoUser;
        JenkinsAdapter.binaryRepoPwd = privateConfig.binaryRepoPwd;

        VCSAdapter.jenkinsUserName = privateConfig.jenkinsUserName;
        VCSAdapter.jenkinsUserPwd = privateConfig.jenkinsUserPwd;
    }

    protected List findProjectPathsFromNames(List list) {
        return list.inject([]) { acc, projName ->
            def projectPaths = getLinuxFilesOnRegex('./src/', "\\./.*/${projName}\\.csproj");
            
            assert projectPaths.size() == 1 : "Project ${projName} in pipeline configuration does not correspond to any existing project";
            acc += projectPaths[0];

            return acc;
        }
    }

    protected void assertCsProjVersionMapNotEmpty() {
        assert this.csprojVersionMap != [:] : "this.csprojVersionMap has not been initialized. See step versioning()";
    }

    protected void assertVersioningSystemInjected() {
        assert this.versioningSystem != null : "this.versioningSystem has not been injected. See injectVersioningSystem()";
    }

    public void injectVersioningSystem(GenericVersioningSystem versioningSystem) {
        this.versioningSystem = versioningSystem;
    }

    public void injectCustomizeStage(String stageType, GenericStage customizedStage) {
        this.customizedStageMap.put(stageType, customizedStage);
        log("new entry in customizedStageMap[${stageType}] -> ${this.customizedStageMap[stageType]}");
    } 

    public void executeStage(String stageName) {
        try {
            Pipeline.metaClass.pickMethod(stageName).doMethodInvoke(this);
        }
        catch (Exception | AssertionError exc) {
            this.rollbackStages();

            throw exc;
        }
    }

    protected GenericStage getCustomStageIfExists(String stageType, GenericStage standardStage) {
        def customizedStage = this.customizedStageMap[stageType];

        def finalStage;
        if (customizedStage == null) {
            finalStage = standardStage;
        }
        else {
            customizedStage.setStage(standardStage);
            log("customized stage initialized");
            
            finalStage = customizedStage;
        }
            
        executedStages.push(finalStage);

        return finalStage; 
    }

    protected void checkout() {
        def standardStage = new CheckoutStage([
            credentialsId: this.privateConfig.credentialsId, 
            repoUrl: this.config.repoUrl
        ]);

        def stage = this.getCustomStageIfExists(PipelineStages.CHECKOUT, standardStage); 
        stage.run();

        this.config.internalProjects = findProjectPathsFromNames(this.config.internalProjects);
        this.config.deployProjects = findProjectPathsFromNames(this.config.deployProjects);

        log("this.config.internalProjects: ${this.config.internalProjects}");
        log("this.config.deployProjects: ${this.config.deployProjects}");
    }

    protected void restore() {
        def standardStage = new RestoreStage([:]);

        def stage = this.getCustomStageIfExists(PipelineStages.RESTORE, standardStage); 
        stage.run();
    }

    protected void clean() {
        def standardStage = new CleanStage([
            slnConfiguration: this.config.slnConfiguration
        ]);

        def stage = this.getCustomStageIfExists(PipelineStages.CLEAN, standardStage); 
        stage.run();
    }

    protected void build() {
        def standardStage = new BuildStage([
            slnConfiguration: this.config.slnConfiguration
        ]);

        def stage = this.getCustomStageIfExists(PipelineStages.BUILD, standardStage); 
        stage.run();
    }

    protected void unitTest() {
        def standardStage = new UnitTestStage([
            slnConfiguration: this.config.slnConfiguration, 
            testResultsPath: this.config.testResultsPath,
            slnName: this.config.slnName,
            dockerVolumeSlaveWorkspace: this.config.dockerVolumeSlaveWorkspace,
            dockerVolumeNugetCache: this.config.dockerVolumeNugetCache,
            dockerImageDotNetSDK: this.config.dockerImageDotNetSDK
        ]);

        def stage = this.getCustomStageIfExists(PipelineStages.UNITTEST, standardStage); 
        stage.run();
    }

    protected void integrationTest() {
        def standardStage = new IntegrationTestStage([
            slnConfiguration: this.config.slnConfiguration, 
            testResultsPath: this.config.testResultsPath,
            slnName: this.config.slnName, 
            environment: this.config.environment
        ]);

        def stage = this.getCustomStageIfExists(PipelineStages.INTEGRATIONTEST, standardStage); 
        stage.run();
    }

    protected void versioning() {
        assertVersioningSystemInjected();

        def allVersioningProjects = [this.config.internalProjects, this.config.deployProjects]
            .flatten()
            .unique();

        log("allVersioningProjects: ${allVersioningProjects}");

        def standardStage = new VersioningStage([
            doesManuallyIncrementMajor: this.config.doesManuallyIncrementMajor, 
            versioningCsProjs: allVersioningProjects,
            versioningSystem: this.versioningSystem
        ]);

        def stage = this.getCustomStageIfExists(PipelineStages.VERSIONING, standardStage); 
        def resultMap = stage.run();
        log("resultMap: ${resultMap}");

        this.isArtifactRequested = resultMap.isArtifactRequested;
        this.csprojVersionMap = resultMap.projectVersionMap;
    }

    protected void pack() {
        assertCsProjVersionMapNotEmpty();
        
        if (!this.isArtifactRequested)
            return;

        def standardStage = new PackStage([
            slnConfiguration: this.config.slnConfiguration, 
            packageFolder: this.config.packageFolder,
            csprojVersionMap: this.csprojVersionMap.subMap(this.config.internalProjects)
        ]);

        def stage = this.getCustomStageIfExists(PipelineStages.PACK, standardStage); 
        stage.run();
    }

    protected void pushPackage() {
        assertCsProjVersionMapNotEmpty();

        if (!this.isArtifactRequested)
            return;

        def standardStage = new PushPackageStage([
            packageFolder: this.config.packageFolder,
            apiKey: this.privateConfig.apiKey,
            csprojVersionMap: this.csprojVersionMap.subMap(this.config.internalProjects),
            internalRepoUrl: this.config.internalRepoUrl
        ]);

        def stage = this.getCustomStageIfExists(PipelineStages.PUSHPACKAGE, standardStage); 
        stage.run();
    }

    protected void publishArtifact() {
        assertCsProjVersionMapNotEmpty();

        if (!this.isArtifactRequested)
            return 

        def standardStage = new PublishArtifactStage([
            slnName: this.config.slnName,
            slnConfiguration: this.config.slnConfiguration, 
            deployFolder: this.config.deployFolder,
            artifactoryServerId: this.config.artifactoryServerId,
            csprojVersionMap: this.csprojVersionMap.subMap(this.config.deployProjects),
            basePathOnBinaryRepo: this.config.basePathOnBinaryRepo
        ]);

        def stage = this.getCustomStageIfExists(PipelineStages.PUBLISHARTIFACT, standardStage); 
        stage.run();
    }

    protected void complete() {
        assertCsProjVersionMapNotEmpty();
        assertVersioningSystemInjected();

        if (!this.isArtifactRequested)
            return 

        def standardStage = new FinalizeStage([
            csprojVersionMap: this.csprojVersionMap,
            versioningSystem: this.versioningSystem
        ]);

        def stage = this.getCustomStageIfExists(PipelineStages.COMPLETE, standardStage); 
        stage.run();
    }

    protected void rollbackStages() {
        while (!executedStages.empty()) {
            def stage = executedStages.pop();

            log("executing rollback for ${stage.class.name}");
            stage.rollback();
        }
    }
}