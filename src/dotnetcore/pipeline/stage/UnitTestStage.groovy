package dotnetcore.pipeline.stage;

import dotnetcore.pipeline.stage.interfaces.TestStage;

import static utils.script.ScriptAdapter.basicSh;

class UnitTestStage extends TestStage {
    String slnName
    String dockerVolumeSlaveWorkspace;
    String dockerVolumeNugetCache;
    String dockerImageDotNetSDK;

    UnitTestStage(Map stageConfigs = [:]) {
        super(stageConfigs);

        this.slnName = stageConfigs.slnName;
        this.dockerVolumeSlaveWorkspace = stageConfigs.dockerVolumeSlaveWorkspace;
        this.dockerVolumeNugetCache = stageConfigs.dockerVolumeNugetCache;
        this.dockerImageDotNetSDK = stageConfigs.dockerImageDotNetSDK;
    }

    def run() {
        Closure runTest = { String testCommand -> basicSh("""
            docker run --rm --network="none" \
            -v ${this.dockerVolumeSlaveWorkspace}:/csproj \
            -v ${this.dockerVolumeNugetCache}:/root/.nuget/packages \
            ${this.dockerImageDotNetSDK} \
            /bin/bash -c \\"cd '/csproj/${this.slnName}' && ${testCommand}\\"
        """)};

        dotnetCliTest('ClassName!~IT_&TestCategory!~IntegrationTest', 'UT', runTest);
    }

    @Override
    void rollback() {
    }
}