package dotnetcore.version3;

def run(Map stageConfigs) {
    try {
        // unit tests
        def containerName = stageConfigs.containerName
        def exitCodeUT = sh(script: """#!/bin/bash -xe
            containerNetworks="\$(docker inspect -f '{{range \$key, \$val := .NetworkSettings.Networks }} {{ \$key }} {{end}}' ${containerName})"
            for network in \$containerNetworks
            do
                docker network disconnect "\$network" "${containerName}"
            done

            dotnet test -c Release \
                --no-build \
                --collect:"XPlat Code Coverage" \
                --filter "ClassName!~IT_&TestCategory!~IntegrationTest" \
                --logger trx \
                -- MSTest.MapInconclusiveToFailed=True

            for network in \$containerNetworks
            do
                docker network connect "\$network" "${containerName}"
            done
        """, returnStatus: true)

        // integration tests
        def exitCodeIT = sh(script: '''
            dotnet test -c Release \
                --no-build \
                --collect:"XPlat Code Coverage" \
                --filter "ClassName~IT_|TestCategory~IntegrationTest" \
                --logger trx \
                -- MSTest.MapInconclusiveToFailed=True
        ''', returnStatus: true)

        def exitCodePT = 0
        def areThereNewmanTests = sh (script: "test -d './test/NewmanTests'", returnStatus: true)
        if (areThereNewmanTests == 0) {
            exitCodePT = sh(script: """
                postman-combine-collections -f './test/NewmanTests/Collections/*.json' \
                    -n 'postman_combined_collections' \
                    -o './test/NewmanTests/postman_combined_collections.json'
                
                newman run './test/NewmanTests/postman_combined_collections.json' \
                    -e './test/NewmanTests/Env/${stageConfigs.environment}.json' \
                    --reporters cli,junit  \
                    --reporter-junit-export './test/NewmanTests/postman_testresults.xml'
            """, returnStatus: true)

            junit allowEmptyResults: true, keepLongStdio: true, testResults: "test/NewmanTests/postman_testresults.xml"
        }

        if (exitCodeUT != 0 || exitCodeIT != 0 || exitCodePT != 0)
            error "Some tests failed... Check test results"
    }
    finally {
        mstest testResultsFile:"**/*.trx", keepLongStdio: true

        sh '''
            reportgenerator \
                    "-reports:./test/*/TestResults/*/coverage.cobertura.xml" \
                    "-targetdir:./coveragereport" \
                    "-reporttypes:Html;Cobertura"
        '''
        cobertura autoUpdateHealth: false, autoUpdateStability: false, coberturaReportFile: '**/coveragereport/*.xml', conditionalCoverageTargets: '50, 20, 0', failNoReports: false, failUnhealthy: false, failUnstable: false, lineCoverageTargets: '50, 20, 0', maxNumberOfBuilds: 0, methodCoverageTargets: '50, 20, 0', onlyStable: false, zoomCoverageChart: false    
    }
}

return this