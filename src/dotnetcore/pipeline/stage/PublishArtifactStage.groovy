package dotnetcore.pipeline.stage;

import interfaces.pipeline.stage.GenericStage;

import static utils.script.ScriptAdapter.*;
import static utils.DateUtils.getFormattedDate;
import static utils.jenkins.JenkinsAdapter.*;

import groovy.json.JsonSlurper;

class PublishArtifactStage extends GenericStage {
    String slnName;
    String slnConfiguration;
    String deployFolder;
    String artifactoryServerId;
    String basePathOnBinaryRepo;

    Map csprojVersionMap;

    List uploadedProjects = [];

    PublishArtifactStage(Map stageConfigs = [:]) {
        super(stageConfigs);

        this.slnName = stageConfigs.slnName;   
        this.slnConfiguration = stageConfigs.slnConfiguration;   
        this.deployFolder = stageConfigs.deployFolder;  
        this.artifactoryServerId = stageConfigs.artifactoryServerId;
        this.csprojVersionMap = stageConfigs.csprojVersionMap;
        this.basePathOnBinaryRepo = stageConfigs.basePathOnBinaryRepo;
    }

    protected String buildUploadSpec(String csproj, String version) {
        def csprojFolder = csproj.substring(0, csproj.lastIndexOf('/'));
        def projectName = getProjectNameFromCsProjPath(csproj);

        def publishDeployFolder = "${csprojFolder}/${this.deployFolder}";
        def zipFileName = "${getFormattedDate(new Date())}_${projectName}_${version}.zip";
        def zipFilePath = "${publishDeployFolder}/${zipFileName}";

        log("publishDeployFolder: ${publishDeployFolder}");
        log("zipFilePath: ${zipFilePath}");

        zip(zipFilePath, publishDeployFolder);
        basicSh("rm -dr ${publishDeployFolder}/dist");

        // Publishing on artifactory
        def uploadSpec = """
            {
                "files": [
                    {
                        "pattern": "${zipFilePath}",
                        "target": "${this.basePathOnBinaryRepo}/${this.slnName}/${projectName}/"
                    }
                ]
            }""";
        
        return uploadSpec;
    }

    def run() {
        if (this.csprojVersionMap.size() == 0)
            return;

        this.csprojVersionMap.each { csproj, versions ->
            def nextVersion = versions.nextVersion;
            basicSh("""dotnet publish ${csproj} \
                -c ${this.slnConfiguration} \
                --no-build --no-restore \
                -o './${this.deployFolder}/dist' \
                --version-suffix '${nextVersion}'
                """);

            // ***** release notes
            // def releaseNoteContent = getReleaseNotesContent();
            // writeFile(file: "./${DEPLOY_FOLDER}/Release_notes.txt", text: releaseNoteContent);
            // ***** 

            def uploadSpec = buildUploadSpec(csproj, nextVersion);
            
            uploadToArtifactory(this.artifactoryServerId, uploadSpec);
            this.uploadedProjects += uploadSpec;
        }
    }

    @Override
    void rollback() {
        this.uploadedProjects.each { uploadSpec ->
            def parsedMap = readJSONFromText(uploadSpec);

            def pattern = parsedMap.files[0].pattern;
            def fileName = pattern.substring(pattern.lastIndexOf('/') + 1);
            def fileLocation = parsedMap.files[0].target + fileName;

            // TODO: check if necessary
            parsedMap = null;

            log("rollback for ${fileLocation}");
            deleteFromArtifactory(this.artifactoryServerId, fileLocation);
        }
    }
}