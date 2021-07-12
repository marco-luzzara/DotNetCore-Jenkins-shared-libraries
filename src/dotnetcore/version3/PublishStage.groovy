package dotnetcore.version3;

def run(Map stageConfigs) {
    def publishingProject = stageConfigs.publishingProject
    println "Publishing project ${publishingProject}"
    def projectName = sh(script: "basename -a --suffix=.csproj ${publishingProject}/*.csproj", returnStdout: true).trim()

    sh """
        dotnet publish ${publishingProject} -c Release --no-build \
            -o '${publishingProject}/bin/deploy/dist' --version-suffix '${env.NEXT_RELEASE}'
    """
}

return this