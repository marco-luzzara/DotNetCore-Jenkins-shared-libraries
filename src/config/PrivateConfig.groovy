package config;

class PrivateConfig implements Serializable {
    final String apiKey;
    final String credentialsId;
    final String binaryRepoUser;
    final String binaryRepoPwd;
    final String jenkinsUserName;
    final String jenkinsUserPwd;

    PrivateConfig(Map configs) {
        this.apiKey = configs.apiKey;
        this.credentialsId = configs.credentialsId;
        this.binaryRepoUser = configs.binaryRepoUser;
        this.binaryRepoPwd = configs.binaryRepoPwd;
        this.jenkinsUserName = configs.jenkinsUserName;
        this.jenkinsUserPwd = configs.jenkinsUserPwd;
    }
}