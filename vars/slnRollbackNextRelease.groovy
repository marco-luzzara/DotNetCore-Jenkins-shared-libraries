def call(Map stageConfigs) {
    def versioner = null
    switch (stageConfigs.version) {
        case "semver-from-git-tag":
            versioner = new versioning.SemVerFromGitTag()
            break
        default:
            throw new RuntimeException("${stageConfigs.version} is not an existing versioning method")
    }

    versioner.rollbackNextRelease(stageConfigs)
}