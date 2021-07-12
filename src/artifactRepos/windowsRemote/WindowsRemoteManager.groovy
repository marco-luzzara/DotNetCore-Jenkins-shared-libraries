package artifactRepos.windowsRemote;

def sendArtifact(Map configs) {
    sh """
        curl --upload-file "${configs.zipFilePath}" -u '$JENKINS_DOMAIN\\$JENKINS_CREDENTIALS' 'smb://${configs.destinationServer}/${configs.destinationFolder}/'
    """
}

def deleteArtifact(Map configs) {
    def projectName = configs.projectName
    def smbUtils = new utils.SMBUtils()
    def tokenizedDestFolder = smbUtils.splitServiceFromDirectory(configs.destinationFolder)
    def serviceName = tokenizedDestFolder[0]
    def initialDirectory = tokenizedDestFolder[1]

    sh """#!/bin/bash -xe
        # TODO: it is possible that the zipName will not be found because the date command returns the following day
        zipName="\$(date +%Y_%m_%d)_${projectName}_${env.NEXT_RELEASE}.zip"
        smbclient -U "$JENKINS_DOMAIN\\\\$JENKINS_CREDENTIALS_USR" -c "del \$zipName" --directory "${initialDirectory}" "//${configs.destinationServer}/${serviceName}" "$JENKINS_CREDENTIALS_PSW"
    """
}

return this