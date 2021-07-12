package artifactRepos.nuget;

def sendArtifact(Map configs) {
    def project = configs.publishingProject
    println "Pushing project ${project} to NuGet"
    sh """
        dotnet pack ${project} -c Release --no-restore --no-build \
            -o ${project}/bin/nupkgs -p:PackageVersion=${env.NEXT_RELEASE}

        dotnet nuget push ${project}/bin/nupkgs/*.nupkg \
            --skip-duplicate -s '${configs.nugetInternalServerUrl}' \
            -k '${configs.nugetApiKey}'
    """
}

def deleteArtifact(Map configs) {
    println "Removing project ${configs.projectName} (version ${configs.nextRelease}) from NuGet"
    sh """
        dotnet nuget delete "${configs.projectName}" ${configs.nextRelease} \
            -k '${configs.nugetApiKey}' --non-interactive \
            -s '${configs.nugetInternalServerUrl}'
    """
}

return this