package dotnetcore.pipeline.stage;

import static utils.script.ScriptAdapter.basicSh;
import interfaces.pipeline.stage.GenericStage;

class RestoreStage extends GenericStage {
    RestoreStage(Map stageConfigs = [:]) {
        super(stageConfigs);
    }

    def run() {
        basicSh('dotnet restore --configfile /root/.config/NuGet/NuGet.Config');
    }

    @Override
    void rollback() {
    }
}