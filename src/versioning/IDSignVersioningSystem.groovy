package versioning;

import versioning.type.SemanticVersionType;
import versioning.interfaces.GenericVersioningSystem;

import static utils.script.ScriptAdapter.*;
import static utils.FileUtils.deleteLinuxFile;
import static utils.RegexUtils.getCGroupsAgainstPattern;

import java.util.regex.Pattern;

class IDSignVersioningSystem extends GenericVersioningSystem {
    final String versioningServerEndpoint;
    final String prjManagePortalUrl;
    final String prjManagePortalUserName;
    final String prjManagePortalUserPwd;

    IDSignVersioningSystem(Map configs = [:]) {
        super(configs);

        this.versioningServerEndpoint = configs.versioningServerEndpoint;
        this.prjManagePortalUrl = configs.prjManagePortalUrl;
        this.prjManagePortalUserName = configs.prjManagePortalUserName;
        this.prjManagePortalUserPwd = configs.prjManagePortalUserPwd;
    }

    private String _getLastVersion(String projectName) {
        def versionHistoryUrl = "${this.versioningServerEndpoint}/${projectName}/versions/last";

        def version = shReturnStdOut("curl '${versionHistoryUrl}'");
        log("version retrieved from ${versionHistoryUrl}: ${version}");
        
        switch (version) {
            case "Project Not found":
                return "";
            default:
                return version;
        }
    }

    private String _getNextVersion(String lastVersion, String taskId, boolean doesIncrementMajor) {
        def uncheckedVersionType = getVersionTypeFromTask(taskId);
        def versionType = uncheckedVersionType == SemanticVersionType.MAJOR ? 
            (doesIncrementMajor ? SemanticVersionType.MAJOR : SemanticVersionType.MINOR) :
            uncheckedVersionType;

        log("Version Type: ${versionType}");

        return lastVersion == "" ? 
            "1.0.0" : 
            SemanticVersionType.computeNextVersion(lastVersion, versionType);
    }

    private SemanticVersionType getVersionTypeFromTask(String taskId) {
        def url = "${this.prjManagePortalUrl}/issues/${taskId}.xml";
        SemanticVersionType vType;

        try {
            def userLogin = "${this.prjManagePortalUserName}:${this.prjManagePortalUserPwd}";
            userLogin = userLogin.replace("'", "'\\''");
            def trackerType = shReturnStdOut("""curl --basic -u '${userLogin}' ${url} > ./redmine_issue.xml && \
                xmllint --xpath \"/issue/custom_fields/custom_field[@name='Release type']/value/text()\" ./redmine_issue.xml""");
                
            switch (trackerType) {
                case 'Major':
                    vType = SemanticVersionType.MAJOR;
                    break;
                case 'Minor':
                    vType = SemanticVersionType.MINOR;
                    break;
                case 'Patch':
                    vType = SemanticVersionType.PATCH;
                    break;
                default:
                    throw new RuntimeException("Release type must be one of the following values: Major, Minor, Patch");
            }
        }
        finally {
            deleteLinuxFile('./redmine_issue.xml');
        }

        return vType;
    }

    def getLastVersion(Maps params = [:]) {
        def lastVersion = this._getLastVersion(params.csProjPath);

        return lastVersion;
    }

    def getNextVersion(Maps params = [:]) {
        throw new IllegalStateException("As for now use getLastAndNextVersion because you are calling getLastVersion anyway, rif. #10185");

        // def lastVersion = this._getLastVersion(params.projectName);
        // def nextVersion = this._getNextVersion(lastVersion, params.taskId, params.doesIncrementMajor);

        // return nextVersion;
    }

    def getLastAndNextVersion(Map params = [:]) {
        def lastVersion = this._getLastVersion(params.projectName);
        def nextVersion = this._getNextVersion(lastVersion, params.taskId, params.doesIncrementMajor);

        return [lastVersion, nextVersion];
    }

    def publishNewVersion(Map params = [:]) {
        def url = "http://${this.versioningServerEndpoint}/${params.projectName}/versions";
        def headerContentType = "Content-Type: application/json";

        def curlRequest = "curl -X POST -d '${params.jsonChanges}' -H \"${headerContentType}\" ${url}";
        basicSh(curlRequest);
    }

    def deleteVersion(Maps params = [:]) {
        def url = "http://${this.versioningServerEndpoint}/${params.projectName}/versions/${params.version}";

        def curlRequest = "curl -X DELETE ${url}";
        basicSh(curlRequest);
    }
}