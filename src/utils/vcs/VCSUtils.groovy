package utils.vcs;

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

return this;