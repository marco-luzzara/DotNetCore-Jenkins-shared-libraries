package versioning;

import versioning.type.SemanticVersionType;
import versioning.interfaces.GenericVersioningSystem;

import static utils.script.ScriptAdapter.*;
import static utils.FileUtils.deleteLinuxFileSafely;
import static utils.RegexUtils.getCGroupsAgainstPattern;

import java.util.regex.Pattern;

class IDSignVersioningSystem extends GenericVersioningSystem {
    final String versioningServerEndpoint;
    final String prjManagePortalUrl;
    final String prjManagePortalUserName;
    final String prjManagePortalUserPwd;

    SemanticVersionType semVersionType = null;
    String taskId = null;

    IDSignVersioningSystem(Map configs = [:]) {
        super(configs);

        this.versioningServerEndpoint = configs.versioningServerEndpoint;
        this.prjManagePortalUrl = configs.prjManagePortalUrl;
        this.prjManagePortalUserName = configs.prjManagePortalUserName;
        this.prjManagePortalUserPwd = configs.prjManagePortalUserPwd;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
        this.semVersionType = getVersionTypeFromTask(taskId);
    }

    private String _getLastVersion(String projectName) {
        def versionHistoryUrl = "${this.versioningServerEndpoint}/${projectName}/versions/last";

        def version = shReturnStdOut("curl '${versionHistoryUrl}'");
        log("version retrieved from ${versionHistoryUrl}: ${version}");
        
        return version;
    }

    private String _getNextVersion(String lastVersion, boolean doesIncrementMajor) {
        assert this.semVersionType != null : "expected call to setTaskId() before computing nextVersion";

        def uncheckedVersionType = this.semVersionType;
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
            deleteLinuxFileSafely('./redmine_issue.xml');
        }

        return vType;
    }

    def getLastVersion(Map params = [:]) {
        def lastVersion = this._getLastVersion(params.csProjPath);

        return lastVersion;
    }

    def getNextVersion(Map params = [:]) {
        throw new IllegalStateException("As for now use getLastAndNextVersion because you are calling getLastVersion anyway, rif. #10185");

        // def lastVersion = this._getLastVersion(params.projectName);
        // def nextVersion = this._getNextVersion(lastVersion, params.doesIncrementMajor);

        // return nextVersion;
    }

    def getLastAndNextVersion(Map params = [:]) {
        def lastVersion = this._getLastVersion(params.projectName);
        def nextVersion = this._getNextVersion(lastVersion, params.doesIncrementMajor);

        return [lastVersion, nextVersion];
    }

    def publishNewVersion(Map params = [:]) {
        def url = "http://${this.versioningServerEndpoint}/${params.projectName}/versions";
        def headerContentType = "Content-Type: application/json";

        def curlRequest = "curl -X POST -d '${params.jsonChanges}' -H \"${headerContentType}\" ${url}";
        basicSh(curlRequest);
    }

    def deleteVersion(Map params = [:]) {
        def url = "http://${this.versioningServerEndpoint}/${params.projectName}/versions/${params.version}";

        def curlRequest = "curl -X DELETE ${url}";
        basicSh(curlRequest);
    }
}