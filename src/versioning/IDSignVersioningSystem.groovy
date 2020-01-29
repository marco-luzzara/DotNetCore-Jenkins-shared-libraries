package versioning;

import versioning.type.SemanticVersionType;
import versioning.interfaces.GenericVersioningSystem;

import static utils.script.ScriptAdapter.*;
import static utils.FileUtils.deleteLinuxFile;
import static utils.RegexUtils.getCGroupsAgainstPattern;

import java.util.regex.Pattern;

class IDSignVersioningSystem extends GenericVersioningSystem {
    final String csProjPath
    final String taskId
    final boolean doesIncrementMajor;
    final String versioningServerEndpoint;
    final String prjManagePortalUrl;
    final String prjManagePortalUserName;
    final String prjManagePortalUserPwd;

    @Lazy private String _lastVersion = {
        def patternForProjName = ~"\\.\\/.*\\/(.*)\\.csproj";
        def projectName = getCGroupsAgainstPattern(this.csProjPath, patternForProjName, 1)[0];
        def versionHistoryUrl = "${this.versioningServerEndpoint}/${projectName}/versions/last";

        def version = shReturnStdOut("curl '${versionHistoryUrl}'");
        log("version retrieved from ${versionHistoryUrl}: ${version}");
        
        switch (version) {
            case "Project Not found":
                return "";
            default:
                return version;
        }
    }();

    @Lazy private String _nextVersion = {
        def uncheckedVersionType = getVersionTypeFromTask();
        def versionType = uncheckedVersionType == SemanticVersionType.MAJOR ? 
            (this.doesIncrementMajor ? SemanticVersionType.MAJOR : SemanticVersionType.MINOR) :
            uncheckedVersionType;

        log("Version Type: ${versionType}");

        return this._lastVersion == "" ? "1.0.0" : SemanticVersionType.computeNextVersion(this._lastVersion, versionType);
    }();

    IDSignVersioningSystem(Map configs = [:]) {
        super(configs);

        this.csProjPath = configs.csProjPath;
        this.taskId = configs.taskId;
        this.doesIncrementMajor = configs.doesIncrementMajor;
        this.versioningServerEndpoint = configs.versioningServerEndpoint;
        this.prjManagePortalUrl = configs.prjManagePortalUrl;
        this.prjManagePortalUserName = configs.prjManagePortalUserName;
        this.prjManagePortalUserPwd = configs.prjManagePortalUserPwd;
    }

    private SemanticVersionType getVersionTypeFromTask() {
        def url = "${this.prjManagePortalUrl}/issues/${this.taskId}.xml";
        SemanticVersionType vType;

        try {
            def userLogin = "${this.prjManagePortalUserName}:${this.prjManagePortalUserPwd}";
            userLogin = userLogin.replace("'", "'\\''");
            def trackerType = shReturnStdOut("""curl --basic -u '${userlogin}' ${url} > ./redmine_issue.xml && \
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

    String getLastVersion() {
        log("_lastVersion: ${this._lastVersion}");
        return this._lastVersion;
    }

    String getNextVersion() {
        log("_nextVersion: ${this._nextVersion}");
        return this._nextVersion;
    }
}