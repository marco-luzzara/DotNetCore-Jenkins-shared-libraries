package interfaces.pipeline.stage;

abstract class GenericStage implements Serializable {
    GenericStage(Map stageConfig = [:]) {}

    abstract def run();
    abstract void rollback();

    protected getProjectNameFromCsProjPath(String csproj) {
        return csproj[(csproj.lastIndexOf('/') + 1)..-8];
    }
}