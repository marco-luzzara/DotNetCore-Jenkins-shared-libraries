package utils.jenkins;

class JenkinsAdapter implements Serializable {
    static Script script;
    static JenkinsUtils adaptee;
    static String binaryRepoUser;
    static String binaryRepoPwd;

    static {
        adaptee = new JenkinsUtils();
    }

    static void zip(String zipFile, String dir) {
        // adaptee.zip(zipFile, dir);
        script.zip(zipFile: zipFile, dir: dir);
    }

    static Map readJSONFromText(String jsonText) {
        return script.readJSON text: jsonText
    }

    static void writeFile(String path, String content) {
        // adaptee.writeFile(path, content);
        script.writeFile(file: path, text: content);
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