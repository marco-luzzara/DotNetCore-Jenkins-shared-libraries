def call(Map stageConfigs) {
    def stage = null
    switch (stageConfigs.version) {
        case "dotnetcore-3":
            stage = new dotnetcore.version3.BuildStage()
            break
        case "angular": 
            stage = new angular.BuildStage()
            break
        default:
            throw new RuntimeException("${stageConfigs.version} is not an existing version for the build stage")
    }

    stage.run(stageConfigs)
}