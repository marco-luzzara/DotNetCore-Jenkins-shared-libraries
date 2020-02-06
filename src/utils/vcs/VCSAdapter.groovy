package utils.vcs;

class VCSAdapter implements Serializable {
    static String jenkinsUserName;
    static String jenkinsUserPwd;
    
    static VCSUtils adaptee;

    static {
        adaptee = new VCSUtils();
    }

    static void gitCheckout(String credentialsId, String repoUrl) {
        adaptee.gitCheckout(credentialsId, repoUrl, jenkinsUserName, jenkinsUserPwd);
    }

    // do not use it when you already have a git repo in the same folder
    static String getFileContentFromVCS(String credentialsId, String filePath, String repoUrl) {
        return adaptee.getFileContentFromVCS(credentialsId, filePath, repoUrl);
    }
}