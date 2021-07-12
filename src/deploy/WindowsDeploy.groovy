package deploy;

def run(Map configs) {
    def smbUtils = new utils.SMBUtils()
    def tokenizedRemoteConfigsFolder = smbUtils.splitServiceFromDirectory(configs.remoteConfigsFolder)
    def remoteConfigsServiceName = tokenizedRemoteConfigsFolder[0]
    def remoteConfigsInitialDirectory = tokenizedRemoteConfigsFolder[1]

    def tokenizedRemoteProjectFolder = smbUtils.splitServiceFromDirectory(configs.remoteProjectFolder)
    def remoteProjectServiceName = tokenizedRemoteProjectFolder[0]
    def remoteProjectInitialDirectory = tokenizedRemoteProjectFolder[1]

    sh """#!/bin/bash -xe
        mkdir -p .deploy/local
        unzip ${configs.zipFilePath} -d .deploy/local

        mkdir -p .deploy/remoteConfigs
        smbclient -U "$JENKINS_DOMAIN\\\\$JENKINS_CREDENTIALS_USR" --directory "${remoteConfigsInitialDirectory}" -c "prompt OFF;recurse ON;lcd .deploy/remoteConfigs;mget *" "//${configs.envEndpoint}/${remoteConfigsServiceName}" "$JENKINS_CREDENTIALS_PSW"
        cp -r .deploy/remoteConfigs/. .deploy/local/${configs.distDirectory}
        smbclient -U "$JENKINS_DOMAIN\\\\$JENKINS_CREDENTIALS_USR" --directory "${remoteProjectInitialDirectory}" -c "deltree dist/*;cd dist; recurse ON;prompt OFF;lcd .deploy/local/${configs.distDirectory};mput *" "//${configs.envEndpoint}/${remoteProjectServiceName}" "$JENKINS_CREDENTIALS_PSW"
    """
}

return this