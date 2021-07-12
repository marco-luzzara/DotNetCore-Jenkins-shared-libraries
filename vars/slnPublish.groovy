def call(Map stageConfigs) {
    def stage = null
    switch (stageConfigs.version) {
        case "dotnetcore-3":
            stage = new dotnetcore.version3.PublishStage()
            break
        case "angular":
            stage = new angular.PublishStage()
            break
        default:
            throw new RuntimeException("${stageConfigs.version} is not an existing version for the publish stage")
    }

    stage.run(stageConfigs)
}