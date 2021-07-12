def call(Map stageConfigs) {
    def stage = null
    switch (stageConfigs.version) {
        case "common":
            stage = new common.InitStage()
            break
        default:
            throw new RuntimeException("${stageConfigs.version} is not an existing version for the init stage")
    }

    stage.run(stageConfigs)
}