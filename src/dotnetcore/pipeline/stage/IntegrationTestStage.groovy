package dotnetcore.pipeline.stage;

import static utils.script.ScriptAdapter.basicSh;
import static utils.jenkins.JenkinsAdapter.publishJUnitReports;

import dotnetcore.pipeline.stage.interfaces.TestStage;

class IntegrationTestStage extends TestStage {
    private final String AGGREGATED_COLLECTION_NAME = '_aggregated_collections.json'
    private final String JUNIT_REPORT_NAME = 'integrationTest_report.xml';
    String slnName
    String environment

    IntegrationTestStage(Map stageConfigs = [:]) {
        super(stageConfigs);

        this.slnName = stageConfigs.slnName;
        this.environment = stageConfigs.environment;
    }

    def run() {
        Closure runTest = { String testCommand -> basicSh(testCommand) };
        dotnetCliTest('ClassName~IT_|TestCategory~IntegrationTest', 'IT', runTest);
        
        basicSh("""postman-combine-collections -f './test/NewmanTests/Collections/*.json' \
            -n '${this.slnName}_all_collections' \
            -o './test/NewmanTests/Collections/${this.AGGREGATED_COLLECTION_NAME}'""");

        try {
            basicSh("""newman run './test/NewmanTests/Collections/${this.AGGREGATED_COLLECTION_NAME}' \
                -e './test/NewmanTests/Env/${this.environment}.json' \
                --reporters cli,junit  \
                --reporter-junit-export './test/NewmanTests/${JUNIT_REPORT_NAME}'""");
        }
        finally {
            publishJUnitReports("test/NewmanTests/${JUNIT_REPORT_NAME}");
        }
    }

    @Override
    void rollback() {
    }
}