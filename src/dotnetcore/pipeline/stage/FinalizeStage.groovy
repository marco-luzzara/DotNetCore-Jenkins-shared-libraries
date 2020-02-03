package dotnetcore.pipeline.stage;

import interfaces.pipeline.stage.GenericStage;

import static utils.FileUtils.*;
import static utils.script.ScriptAdapter.*;
import static utils.RegexUtils.getCGroupsAgainstPattern;

import java.util.regex.Pattern;
import groovy.json.JsonOutput;

class FinalizeStage extends GenericStage {
    String versioningServerEndpoint;
    Map csprojVersionMap;

    FinalizeStage(Map stageConfig = [:]) {
        super(stageConfig);

        this.versioningServerEndpoint = stageConfig.versioningServerEndpoint;
        this.csprojVersionMap = stageConfig.csprojVersionMap;
    }
    
    protected String getLastTagCommit(String projPath) {
        // lastVersion corresponds to last tag
        def lastVersion = this.csprojVersionMap[projPath].lastVersion;

        def commitOfLastVersion = "";
        if (lastVersion == "")
            commitOfLastVersion = null;
        else
            commitOfLastVersion = shReturnStdOut("git rev-list -n 1 ${lastVersion}").trim();

        log("commitOfLastVersion: ${commitOfLastVersion}");
        return commitOfLastVersion;
    }

    protected String[] getSubsetOfCommits(String excludedstartingCommit) {
        def includeCommit = false;
        if (excludedstartingCommit == null) {
            excludedstartingCommit = shReturnStdOut("git rev-list --max-parents=0 HEAD").trim();
            includeCommit = true;
        }

        def commits = shReturnStdOut("git rev-list ${excludedstartingCommit}..HEAD")
            .trim().split("\n") as List;

        if (includeCommit)
            commits.push(excludedstartingCommit);

        log("commits: ${commits}");
        return commits;
    }

    protected Map getNewVersionChanges(String projPath) {
        def lastVersionCommit = this.getLastTagCommit(projPath);
        def commitsFromLastVersion = this.getSubsetOfCommits(lastVersionCommit);
        log("commitsFromLastVersion: ${commitsFromLastVersion}");

        def nextVersion = this.csprojVersionMap[projPath].nextVersion;
        Map changes = [version: nextVersion];

        def taskMap = [:];
        
        log("commits size: ${commitsFromLastVersion.size()}");
        for (commit in commitsFromLastVersion) {
            log("commit: ${commit}");
            def commitMsg = shReturnStdOut("git show --quiet --format='%s' ${commit}").trim();

            def patternForTaskNumFromMsg = ~"^(?:[rR]if\\.? +)?#(\\d+) *(?::|-| )";
            def cgroupsFromCommitMsg = getCGroupsAgainstPattern(commitMsg, patternForTaskNumFromMsg, 1);

            def task = cgroupsFromCommitMsg[0];
            log("task: ${task}");

            if (taskMap.containsKey(task))
                taskMap[task] += commitMsg;
            else
                taskMap[task] = [commitMsg];
        }

        log("taskMap: ${taskMap}");

        def taskList = [];
        taskMap.each { task, commitMsgList -> 
            def taskEntry = [
                taskId: task,
                commits: commitMsgList
            ];

            taskList += taskEntry;
        };

        changes.put("tasks", taskList);

        log("changes: ${changes}");
        return changes;
    }

    protected void updateRepoTagging() {
        // get first entry: it should be verified that all csproj versions are the same
        def nextVersion = this.csprojVersionMap.find().value.nextVersion;

        basicSh("git tag -a -m '${nextVersion}' ${nextVersion} HEAD");
        basicSh("git push --follow-tags");
    }

    protected void rollbackRepoTagging() {
        // get first entry: it should be verified that all csproj versions are the same
        def nextVersion = this.csprojVersionMap.find().value.nextVersion;
        
        basicSh("git tag -d ${nextVersion} || true");
        basicSh("git push --delete origin ${nextVersion} || true");
    }

    protected void rollbackVersioning() {
        this.csprojVersionMap.each { csproj, versions ->
            log("csproj: ${csproj}");
            log("versions: ${versions}");

            def projectName = this.getProjectNameFromCsProjPath(csproj);
            def version = versions.nextVersion;

            def url = "http://${this.versioningServerEndpoint}/${projectName}/versions/${version}";

            def curlRequest = "curl -X DELETE ${url}";
            basicSh(curlRequest);
        }
    }

    protected void updateVersioningServer() {
        this.csprojVersionMap.each { csproj, versions ->
            log("csproj: ${csproj}");
            log("versions: ${versions}");

            def changes = this.getNewVersionChanges(csproj);
            def jsonChanges = JsonOutput.toJson(changes);
            jsonChanges = jsonChanges.replace("'", "'\\''");

            log("jsonChanges: ${jsonChanges}");
            def projectName = this.getProjectNameFromCsProjPath(csproj);

            def url = "http://${this.versioningServerEndpoint}/${projectName}/versions";
            def headerContentType = "Content-Type: application/json";

            def curlRequest = "curl -X POST -d '${jsonChanges}' -H \"${headerContentType}\" ${url}";
            basicSh(curlRequest);
        }
    }

    @Override
    def run() {
        updateVersioningServer();
        updateRepoTagging();
    }

    @Override
    void rollback() {
        rollbackRepoTagging();
        rollbackVersioning();
    }
}