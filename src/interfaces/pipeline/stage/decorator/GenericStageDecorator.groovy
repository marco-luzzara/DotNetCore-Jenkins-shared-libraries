package interfaces.pipeline.stage.decorator;

import interfaces.pipeline.stage.GenericStage;

abstract class GenericStageDecorator extends GenericStage {
    protected GenericStage baseStage;

    public GenericStageDecorator(Map stageConfig = [:]) {
        super(stageConfig);
    }

    public void setStage(GenericStage baseStage) {
        this.baseStage = baseStage;
    }

    protected void assertStageNotNull() {
        assert baseStage != null : "Missing call to setStage before calling other methods";
    }

    @Override
    def run() {
        assertStageNotNull();

        baseStage.run();
    }

    @Override
    void rollback() {
        assertStageNotNull();

        baseStage.rollback();
    } 
}