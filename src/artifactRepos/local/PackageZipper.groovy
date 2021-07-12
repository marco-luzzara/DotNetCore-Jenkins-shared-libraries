package artifactRepos.local;

/**
 * Save the zip of the package to deploy in a temp folder and return its path
 */
def saveArtifact(Map configs) {
    def suffix = configs.zipSuffix ?: ''
    def projectName = configs.projectName
    def zipFileName = sh(script: """
        echo "\$(date +%Y_%m_%d)_${projectName}_${env.NEXT_RELEASE}${suffix}.zip"
    """, returnStdout: true).trim()

    println "Creating zip for ${configs.projectName}"
    def zipUtils = new utils.ZipUtils();
    def zipFilePath = zipUtils.zipFolders(zipFileName, configs.folderMappings)

    return zipFilePath
}

return this