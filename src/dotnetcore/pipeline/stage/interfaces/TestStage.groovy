package dotnetcore.pipeline.stage.interfaces;

import interfaces.pipeline.stage.GenericStage;

import static utils.FileUtils.getLinuxFilesOnRegex;
import static utils.script.ScriptAdapter.*;
import static utils.DateUtils.getFormattedTimeStamp;
import static utils.jenkins.JenkinsAdapter.castDotNetToJUnitTestReport;

abstract class TestStage extends GenericStage {
    String slnConfiguration
    String testResultsPath

    TestStage(Map stageConfig = [:]) {
        super(stageConfig);

        this.slnConfiguration = stageConfig.slnConfiguration;
        this.testResultsPath = stageConfig.testResultsPath;
    }

    protected String sanitizeTestResultsPath(String testResultsPath) {
        return testResultsPath.startsWith("./") ? testResultsPath.substring(2) : testResultsPath;
    }

    def dotnetCliTest(String filter, String type) {
        def testProjects = getLinuxFilesOnRegex('./test/', '\\./.*/.*\\.csproj');
        def currentDate = new Date();
        def formattedTimeStamp = getFormattedTimeStamp(currentDate);

        for (testProj in testProjects) {
            log("tests for project ${testProj}");

            def testProjName = getProjectNameFromCsProjPath(testProj);
            def logFileName = "${type}+${testProjName}_${formattedTimeStamp}.trx"

            try {
                basicSh("""dotnet test ${testProj} \
                    -c ${this.slnConfiguration} \
                    --no-build \
                    --no-restore \
                    --logger 'trx;logfilename=${logFileName}' \
                    -r ${this.testResultsPath} \
                    --filter '${filter}' \
                    -- MSTest.MapInconclusiveToFailed=True
                    """);
            }
            finally {
                castDotNetToJUnitTestReport("test/${testProjName}/${sanitizeTestResultsPath(this.testResultsPath)}/${logFileName}");
            }
        }
    };
}