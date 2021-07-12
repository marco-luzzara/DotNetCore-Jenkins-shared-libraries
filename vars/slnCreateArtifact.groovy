def call(Map stageConfigs) {
    def stage = null
    switch (stageConfigs.version) {
        case "zipped":
            stage = new artifactRepos.local.PackageZipper()
            break
        default:
            throw new RuntimeException("${stageConfigs.version} is not an existing version for the create artifact stage")
    }

    stage.saveArtifact(stageConfigs)
}