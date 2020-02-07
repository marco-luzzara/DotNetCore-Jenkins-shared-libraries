package utils.jenkins;

class JenkinsAdapter implements Serializable {
    static JenkinsUtils adaptee;
    static String binaryRepoUser;
    static String binaryRepoPwd;

    static {
        adaptee = new JenkinsUtils();
    }

    static void zip(String zipFile, String dir) {
        adaptee._zip(zipFile, dir);
    }

    static Map readJSONFromText(String jsonText) {
        return adaptee.readJSONFromText(jsonText);
    }

    static void writeFile(String path, String content) {
        adaptee._writeFile(path, content);
    }

    static void uploadToArtifactory(String artifactoryServerId, String uploadSpec) {
        adaptee.uploadToArtifactory(artifactoryServerId, uploadSpec);
    }

    // fileOrFolderPath does not start with /
    static void deleteFromArtifactory(String artifactoryServerId, String fileOrFolderPath) {
        adaptee.deleteFromArtifactory(artifactoryServerId, fileOrFolderPath, 
            JenkinsAdapter.binaryRepoUser, JenkinsAdapter.binaryRepoPwd);
    }

    static void castDotNetToJUnitTestReport(String trxPath) {
        adaptee.castDotNetToJUnitTestReport(trxPath);
    }

    static void publishJUnitReports(String reportPath) {
        adaptee.publishJUnitReports(reportPath);
    }
}