def call(Map stageConfigs) {
    def rollbackProjectEntry = "${stageConfigs.projectToRollback}_${stageConfigs.version}"
    if (!(rollbackProjectEntry in stageConfigs.alreadyPublishedProjects))
        return

    def stage = null
    switch (stageConfigs.version) {
        case "artifactory":
            stage = new artifactRepos.artifactory.ArtifactoryManager()
            break
        case "windowsRemote":
            stage = new artifactRepos.windowsRemote.WindowsRemoteManager()
            break
        case "nuget":
            stage = new artifactRepos.nuget.NugetManager()
            break
        default:
            throw new RuntimeException("${stageConfigs.version} is not an existing version for the delete artifact stage")
    }

    try {
        stage.deleteArtifact(stageConfigs)
    }
    catch (e) {
        notifyWarning(version: stageConfigs.notifier, message: e.message, 
            additionalInfo: e.stackTrace.join('\n'), slackResponse: configs.slackResponse)
    }
}