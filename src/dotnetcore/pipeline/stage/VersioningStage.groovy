package dotnetcore.pipeline.stage;

import static utils.script.ScriptAdapter.*;
import static utils.FileUtils.getLinuxFilesOnRegex;
import static utils.RegexUtils.getCGroupsAgainstPattern;

import interfaces.pipeline.stage.GenericStage;
import versioning.IDSignVersioningSystem;
import versioning.interfaces.GenericVersioningSystem;

import java.util.regex.Pattern;

class VersioningStage extends GenericStage {
    boolean doesManuallyIncrementMajor;
    String versioningServerEndpoint;
    List versioningCsProjs;
    String prjManagePortalUrl;
    String prjManagePortalUserName;
    String prjManagePortalUserPwd;

    GenericVersioningSystem versioningSystem;

    VersioningStage(Map stageConfigs = [:]) {
        super(stageConfigs);

        this.doesManuallyIncrementMajor = stageConfigs.doesManuallyIncrementMajor;  
        this.versioningServerEndpoint = stageConfigs.versioningServerEndpoint;
        this.versioningCsProjs = stageConfigs.versioningCsProjs;
        this.prjManagePortalUrl = stageConfigs.prjManagePortalUrl;
        this.prjManagePortalUserName = stageConfigs.prjManagePortalUserName;
        this.prjManagePortalUserPwd = stageConfigs.prjManagePortalUserPwd;
    }

    private String getLastCommitMsg() {
        def commitMsg = shReturnStdOut("git log -n 1 --format='%s'").trim();
        log("gitCommitMsg: ${commitMsg}");

        return commitMsg;
    }
    
    private Map getTaskIdAndTagFromCommitMsg(String commitMsg) {
        def patternForTaskNumFromMsg = ~"^(?:[rR]if\\.? +)?#(\\d+) *(?::|-| ) *(?:\\{(no-artifact|major)\\})?"                                          
        def cgroupsFromCommitMsg = getCGroupsAgainstPattern(commitMsg, patternForTaskNumFromMsg, 2);

        def returnMap = [
            taskId: cgroupsFromCommitMsg[0], 
            isArtifactRequested: cgroupsFromCommitMsg[1] == 'no-artifact' ? false : true,
            doesIncrementMajor: cgroupsFromCommitMsg[1] == 'major' ? true : false
        ];

        log("taskId: ${returnMap.taskId}");
        log("isArtifactRequested: ${returnMap.isArtifactRequested}");
        log("doesIncrementMajor: ${returnMap.doesIncrementMajor}");

        return returnMap;
    }

    protected void handleDisalignedProjects(Map csprojVersionMap) {
        def firstVersionedPrj = csprojVersionMap.find { entry ->
            return entry.value.lastVersion != "";
        }

        if (firstVersionedPrj != null) {
            csprojVersionMap.each { entry ->
                if (entry.value.lastVersion == "") {
                    entry.value.lastVersion = firstVersionedPrj.value.lastVersion;
                    entry.value.nextVersion = firstVersionedPrj.value.nextVersion;
                }
            }
        }

        assert csprojVersionMap.entrySet()
            .collect({ it.value.nextVersion })
            .stream().distinct().count() == 1 : "versions are misaligned";
    }

    // return 
    // [
    //     "isArtifactRequested": true,
    //     "projectVersionMap": [
    //         "proj1": [  
    //             lastVersion: "1.0.0", 
    //             nextVersion: "1.0.1"
    //         ],
    //         "proj2": [  
    //             lastVersion: null, 
    //             nextVersion: "1.0.0"
    //         ],
    //         ...
    //     ]
    // ]
    
    // or 
    
    // [
    //     "isArtifactRequested": false,
    //     "projectVersionMap": []
    // ]
    def run() {
        assert this.versioningCsProjs.size() != 0 : "Specify what projects you want to version before executing this step";

        def returnMap = [:];        

        def csprojs = getLinuxFilesOnRegex('./src/', "\\./.*/.*\\.csproj") as List;

        def lastCommitMsg = this.getLastCommitMsg();
        def commitMsgParsed = this.getTaskIdAndTagFromCommitMsg(lastCommitMsg);

        def doesIncrementMajor = commitMsgParsed.doesIncrementMajor || this.doesManuallyIncrementMajor;
        def isArtifactRequested = commitMsgParsed.isArtifactRequested;
        returnMap.put("isArtifactRequested", isArtifactRequested);

        def csprojVersionMap = [:];
        if (isArtifactRequested) {
            for (csproj in csprojs.intersect(this.versioningCsProjs)) {
                log("csproj: ${csproj}");

                this.versioningSystem = new IDSignVersioningSystem([
                    csProjPath: csproj,
                    taskId: commitMsgParsed.taskId,
                    doesIncrementMajor: doesIncrementMajor,
                    versioningServerEndpoint: this.versioningServerEndpoint,
                    prjManagePortalUrl: this.prjManagePortalUrl,
                    prjManagePortalUserName: this.prjManagePortalUserName,
                    prjManagePortalUserPwd: this.prjManagePortalUserPwd
                ]);

                def lastVersion = this.versioningSystem.getLastVersion();
                def nextVersion = this.versioningSystem.getNextVersion();

                log("lastVersion: ${lastVersion}");
                log("nextVersion: ${nextVersion}");

                csprojVersionMap.put(csproj, [
                    lastVersion: lastVersion, 
                    nextVersion: nextVersion
                ]);
            }
        }

        handleDisalignedProjects(csprojVersionMap);

        returnMap.put("projectVersionMap", csprojVersionMap);
        log("csprojVersionMap: ${csprojVersionMap}");

        return returnMap;
    }

    @Override
    void rollback() {
    }
}