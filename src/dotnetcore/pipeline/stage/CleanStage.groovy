package dotnetcore.pipeline.stage;

import static utils.script.ScriptAdapter.basicSh;
import interfaces.pipeline.stage.GenericStage;

class CleanStage extends GenericStage {
    String slnConfiguration

    CleanStage(Map stageConfigs = [:]) {
        super(stageConfigs);

        this.slnConfiguration = stageConfigs.slnConfiguration;  
    }

    def run() {        
        basicSh("dotnet clean -c ${this.slnConfiguration}");
    }

    @Override
    void rollback() {
    }
}