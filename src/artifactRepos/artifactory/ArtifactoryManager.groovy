package artifactRepos.artifactory;

def sendArtifact(Map configs) {
    def uploadSpec = """
        {
            "files": [
                {
                    "pattern": "${configs.zipFilePath}",
                    "target": "${configs.projectGroup}/${env.SOLUTION_NAME}/${configs.projectName}/"
                }
            ]
        }""";
    
    def server = Artifactory.server configs.artifactoryServerId
    server.upload spec: uploadSpec, failNoOp: true
}

def deleteArtifact(Map configs) {
    def server = Artifactory.server configs.artifactoryServerId
    def serverUrl = server.url;

    sh """#!/bin/bash -xe
        zipName="\$(date +%Y_%m_%d)_${configs.projectName}_${env.NEXT_RELEASE}.zip"
        url="${serverUrl}/${configs.projectGroup}/$SOLUTION_NAME/${configs.projectName}/\$zipName"
        curl -X DELETE -u "$JENKINS_CREDENTIALS" "\$url"
    """
}

return this