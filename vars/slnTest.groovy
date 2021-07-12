def call(Map stageConfigs) {
    def stage = null
    switch (stageConfigs.version) {
        case "dotnetcore-3":
            stage = new dotnetcore.version3.TestStage()
            break
        case "angular":
            stage = new angular.TestStage()
            break
        default:
            throw new RuntimeException("${stageConfigs.version} is not an existing version for the test stage")
    }

    stage.run(stageConfigs)
}