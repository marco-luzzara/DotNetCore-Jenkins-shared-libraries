package globalUtils.pipeline;

import config.*;
import dotnetcore.pipeline.Pipeline;
import static utils.vcs.VCSAdapter.getFileContentFromVCS;

def getPipelinePrivateConfig(String credentialsId, String fileName) {
    def filePath = "config/private/${fileName}";
    def repoUrl = 'https://redmine.aliaslab.net/git/prj-00858.git';

    def stringedCredentials = getFileContentFromVCS(credentialsId, filePath, repoUrl);

    def parsedMap = readJSON text: stringedCredentials
    parsedMap.credentialsId = credentialsId;

    println("parsedMap private config: ${parsedMap}");

    return parsedMap;
}

def getPipelineConfig(String pipelineConfigJson, boolean isManualBuild, String credentialsId) {
    if (!isManualBuild) {
        def filePath = "config/IDSign/${env.JOB_BASE_NAME}.json";
        def repoUrl = 'https://redmine.aliaslab.net/git/prj-00858.git';

        def pipelineConfigFromRepo = getFileContentFromVCS(credentialsId, filePath, repoUrl);
        pipelineConfigJson = pipelineConfigFromRepo;
    }

    println("pipelineConfigJson: ${pipelineConfigJson}");

    def parsedMap = readJSON text: pipelineConfigJson
    parsedMap.slnName = env.JOB_BASE_NAME;

    println("parsedMap config: ${parsedMap}");

    return parsedMap;
}

// json config for pipeline
def createDotNetCorePipeline(Map configs, Map privateConfigs) {
    def pipelineConfigs = new PipelineConfig(configs);
    def pipelinePrivateConfigs = new PrivateConfig(privateConfigs);

    def pipeline = new Pipeline(pipelineConfigs, pipelinePrivateConfigs);

    return pipeline;
}