package globalUtils.pipeline;

import config.*;
import dotnetcore.pipeline.Pipeline;

def getPipelinePrivateConfig(Strig credentialsId, String fileName) {
    def fileNamePath = "config/private/${fileName}";

    checkout([
        $class: 'GitSCM', 
        branches: [[name: '*/master']], 
        doGenerateSubmoduleConfigurations: false, 
        extensions: [
            [
                $class: 'SparseCheckoutPaths', 
                sparseCheckoutPaths: [[path: fileNamePath]]
            ]
        ], 
        submoduleCfg: [], 
        userRemoteConfigs: [
            [
                credentialsId: credentialsId, 
                url: 'https://redmine.aliaslab.net/git/prj-00858.git'
            ]
        ]
    ])

    def stringedCredentials = readFile file: fileNamePath

    def parsedMap = readJSON text: stringedCredentials
    parsedMap.credentialsId = credentialsId;

    println("parsedMap private config: ${parsedMap}");

    sh 'rm -r config'

    return parsedMap;
}

def getPipelineConfig(String pipelineConfigJson) {
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

    def pipeline = new Pipeline(pipelineConfigs, pipelinePrivateConfigs, this);

    return pipeline;
}