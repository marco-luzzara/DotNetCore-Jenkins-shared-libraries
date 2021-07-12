def call(Map stageConfigs) {
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
            throw new RuntimeException("${stageConfigs.version} is not an existing version for the send artifact stage")
    }

    stage.sendArtifact(stageConfigs)
    def publishedProjectEntry = "${stageConfigs.publishingProject}_${stageConfigs.version}"
    stageConfigs.alreadyPublishedProjects.add(publishedProjectEntry)
}