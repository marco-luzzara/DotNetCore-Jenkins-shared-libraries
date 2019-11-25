package dotnetcore.pipeline.stage;

import static utils.script.ScriptAdapter.*;

import interfaces.pipeline.stage.GenericStage;

class PackStage extends GenericStage {
    String slnConfiguration;
    String packageFolder;
    Map csprojVersionMap;

    PackStage(Map stageConfigs = [:]) {
        super(stageConfigs);

        this.slnConfiguration = stageConfigs.slnConfiguration;   
        this.packageFolder = stageConfigs.packageFolder;   
        this.csprojVersionMap = stageConfigs.csprojVersionMap;   
    }

    def run() {
        this.csprojVersionMap.each { csproj, versions ->
            log("csproj: ${csproj}");

            basicSh("""dotnet pack ${csproj} \
                -c ${this.slnConfiguration} \
                --no-restore --no-build \
                -o ./${this.packageFolder} \
                -p:PackageVersion=${versions.nextVersion}""");
        }
    }

    @Override
    void rollback() {
    }
}