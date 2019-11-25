package dotnetcore.pipeline.stage;

import interfaces.pipeline.stage.GenericStage;

import static utils.script.ScriptAdapter.*;
import static utils.FileUtils.getLinuxFilesOnRegex;
import static utils.RegexUtils.getCGroupsAgainstPattern;

class PushPackageStage extends GenericStage {
    String packageFolder
    String apiKey;
    Map csprojVersionMap;
    String internalRepoUrl;

    List pushedNupkgs = [];

    PushPackageStage(Map stageConfigs = [:]) {
        super(stageConfigs);

        this.packageFolder = stageConfigs.packageFolder;   
        this.apiKey = stageConfigs.apiKey;
        this.csprojVersionMap = stageConfigs.csprojVersionMap;
        this.internalRepoUrl = stageConfigs.internalRepoUrl;
    }

    def run() {   
        if (this.csprojVersionMap.size() == 0)
            return;

        def nupkgs = getLinuxFilesOnRegex('./src/', "\\./.*/${this.packageFolder}/.*\\.nupkg");

        for (nupkg in nupkgs) {
            log("nupkg file: ${nupkg}");

            this.pushedNupkgs += nupkg;
            basicSh("""dotnet nuget push ${nupkg} \
                -k '${this.apiKey}' \
                -s '${this.internalRepoUrl}'""");
        }
    }

    @Override
    void rollback() {
        def csprojPathFromNupgkPattern = ~"(.*)\\/${this.packageFolder}\\/(.*)\\.\\d+\\.\\d+\\.\\d+\\.nupkg";

        this.pushedNupkgs.each { nupkg ->
            def csprojPathMatches = getCGroupsAgainstPattern(nupkg, csprojPathFromNupgkPattern, 2);

            def projectName = csprojPathMatches[1];
            def csprojPath = "${csprojPathMatches[0]}/${projectName}.csproj";

            def projVersion = this.csprojVersionMap[csprojPath].nextVersion;

            log("rollback for ${nupkg}");
            basicSh("""dotnet nuget delete ${projectName} ${projVersion} \
                -k '${this.apiKey}' \
                --non-interactive \
                -s '${this.internalRepoUrl}' || true""");
        }
    }
}