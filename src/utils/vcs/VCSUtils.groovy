package utils.vcs;

import static utils.FileUtils.deleteLinuxFileOrDir;

def gitCheckout(String credentialsId, String repoUrl, String jenkinsUserName, String jenkinsUserPwd) {
    checkout([
        $class: 'GitSCM', branches: [[name: '*/master']], 
        doGenerateSubmoduleConfigurations: false, 
        extensions: [[$class: 'WipeWorkspace']], 
        submoduleCfg: [], 
        userRemoteConfigs: [[credentialsId: credentialsId, url: repoUrl]]
    ]);

    sh 'git checkout master'
    sh "git config --global user.name '${jenkinsUserName}'"

    def pwdEscapedQuote = jenkinsUserPwd.replace("'", "'\\''");
    sh "git config --global user.password '${pwdEscapedQuote}'"
    sh 'git config --global credential.helper store'

    def pwdEscapedQuoteAndAt = pwdEscapedQuote.replace('@', '%40');
    def credentials = "${jenkinsUserName}:${pwdEscapedQuoteAndAt}";
    def originUrl = "https://${credentials}@${repoUrl[8..-1]}";
    sh "git config remote.origin.url '${originUrl}'";
}

def getFileContentFromVCS(String credentialsId, String filePath, String repoUrl) {
    checkout([
        $class: 'GitSCM', 
        branches: [[name: '*/master']], 
        doGenerateSubmoduleConfigurations: false, 
        extensions: [
            [
                $class: 'SparseCheckoutPaths', 
                sparseCheckoutPaths: [[path: filePath]]
            ]
        ], 
        submoduleCfg: [], 
        userRemoteConfigs: [
            [
                credentialsId: credentialsId, 
                url: repoUrl
            ]
        ]
    ])

    def fileContent = readFile file: filePath
    
    deleteLinuxFileOrDir(filePath);
    deleteLinuxFileOrDir('.git');

    return fileContent
}

return this;