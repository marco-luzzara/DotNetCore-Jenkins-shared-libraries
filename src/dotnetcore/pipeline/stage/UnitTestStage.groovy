package dotnetcore.pipeline.stage;

import dotnetcore.pipeline.stage.interfaces.TestStage;

class UnitTestStage extends TestStage {
    UnitTestStage(Map stageConfigs = [:]) {
        super(stageConfigs);
    }

    def run() {
        dotnetCliTest('ClassName!~IT_&TestCategory!~IntegrationTest', 'UT');      
    }

    @Override
    void rollback() {
    }
}