package dotnetcore.pipeline.stage;

import static utils.script.ScriptAdapter.basicSh;
import interfaces.pipeline.stage.GenericStage;

class BuildStage extends GenericStage {
    String slnConfiguration

    BuildStage(Map stageConfigs = [:]) {
        super(stageConfigs);

        this.slnConfiguration = stageConfigs.slnConfiguration;  
    }

    def run() {        
        basicSh("dotnet build . -c ${this.slnConfiguration}");
    }

    @Override
    void rollback() {
    }
}