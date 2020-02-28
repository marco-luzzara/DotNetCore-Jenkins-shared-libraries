package config;

class PipelineConfig implements Serializable {
    final String slnConfiguration;
    final String slnName;
    final String environment;
    final boolean doesManuallyIncrementMajor;
    final String packageFolder;
    final String deployFolder;
    final String repoUrl;
    final String testResultsPath;
    final String versioningServerEndpoint;
    final String internalRepoUrl;
    final String prjManagePortalUrl;
    final String artifactoryServerId;
    final String basePathOnBinaryRepo;
    final String dockerVolumeSlaveWorkspace;
    final String dockerVolumeNugetCache;
    final String dockerImageDotNetSDK;
    List internalProjects;
    List deployProjects;

    PipelineConfig(Map configs) {
        this.slnConfiguration = configs.slnConfiguration;
        this.slnName = configs.slnName;
        this.environment = configs.environment;
        this.doesManuallyIncrementMajor = configs.doesManuallyIncrementMajor;
        this.packageFolder = configs.packageFolder;
        this.deployFolder = configs.deployFolder;
        this.repoUrl = configs.repoUrl;
        this.testResultsPath = configs.testResultsPath ?: './TestResults';
        this.versioningServerEndpoint = configs.versioningServerEndpoint;
        this.internalRepoUrl = configs.internalRepoUrl;
        this.prjManagePortalUrl = configs.prjManagePortalUrl;
        this.artifactoryServerId = configs.artifactoryServerId;
        this.basePathOnBinaryRepo = configs.basePathOnBinaryRepo;
        this.dockerVolumeSlaveWorkspace = configs.dockerVolumeSlaveWorkspace;
        this.dockerVolumeNugetCache = configs.dockerVolumeNugetCache;
        this.dockerImageDotNetSDK = configs.dockerImageDotNetSDK;
        this.internalProjects = configs.internalProjects;
        this.deployProjects = configs.deployProjects;
    }
}