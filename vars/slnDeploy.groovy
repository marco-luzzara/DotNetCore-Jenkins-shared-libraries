def call(Map stageConfigs) {
    def stage = null
    switch (stageConfigs.version) {
        case "windows":
            stage = new deploy.WindowsDeploy()
            break
        default:
            throw new RuntimeException("${stageConfigs.version} is not an existing version for the deploy stage")
    }

    stage.run(stageConfigs)
}