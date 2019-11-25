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
}