
package utils.jenkins;

def zip(String _zipFile, String _dir) {
    zip zipFile: _zipFile, dir: _dir
}

def uploadToArtifactory(String artifactoryServerId, String uploadSpec) {
    def server = Artifactory.server artifactoryServerId
    server.upload spec: uploadSpec, failNoOp: true
}

def deleteFromArtifactory(String artifactoryServerId, String fileOrFolderPath, String user, String pwd) {
    def server = Artifactory.server artifactoryServerId
    def serverUrl = server.url;

    def url = "${serverUrl}/${fileOrFolderPath}";

    sh "curl -X DELETE -u '${user}:${pwd}' '${url}'"
}

def writeFile(String path, String content) {
    writeFile file: path, text: content
}

def castDotNetToJUnitTestReport(String trxPath) {
    mstest testResultsFile:"${trxPath}", keepLongStdio: true
}

def publishJUnitReports(String reportPath) {
    junit allowEmptyResults: true, keepLongStdio: true, testResults: reportPath
}

return this;