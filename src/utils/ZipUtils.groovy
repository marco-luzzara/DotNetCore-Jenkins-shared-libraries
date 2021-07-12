package utils;

/**
 * compress many folders using user-custom mapping and return the filepath of the zip created
 * @param zipName the name of the zip to create
 * @param folderMappings mapping for each folder (first item) to the path inside the zip (second item), like
        ["src/myprj1/bin", "."] // bin folder is mapped as root inside the zipped folder
        ["src/myprj2/test", "./configs"] // test folder is copied in the zip and renamed as configs
        ["", "./to/delete"] // delete to/delete folder (or file) in the zipped folder
        ["src/prj1/my_binary", "./bin"] // copy file my_binary inside bin in zipped folder
   @return String the path of the zip file (file zip included)
 */
def zipFolders(String zipName, List<List<String>> folderMappings) {
    def cpCommands = ""
    folderMappings.each {
        cpCommands += """
            if [[ "${it[0]}" == "" ]]
            then
                rm -rf "\$tempFolder/${it[1]}"
            else
                mkdir -p "\$tempFolder/${it[1]}"
                if [[ -f "${it[0]}" ]]
                then
                    cp "${it[0]}" "\$tempFolder/${it[1]}"
                else
                    cp -a "${it[0]}/." "\$tempFolder/${it[1]}"
                fi
            fi
        """
    }

    def zipPath = sh(script: """#!/bin/bash -xe
        tempFolder=".jenkins"
        mkdir -p "\$tempFolder"
        ${cpCommands}        
        (cd "\$tempFolder"; zip -qr "${zipName}" .)
        echo "\$tempFolder/${zipName}"
    """, returnStdout: true).trim()

    return zipPath
}

return this