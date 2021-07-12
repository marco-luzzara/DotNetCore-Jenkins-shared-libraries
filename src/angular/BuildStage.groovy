package angular;

def run(Map stageConfigs) {
    sh """#!/bin/bash -xe
        function getNodeModulesListHash {
            npm ls 2> /dev/null | md5sum | cut -d ' ' -f 1
        }

        cd "${stageConfigs.frontendProject}"
        frontendProjectHashZip="\$(echo "${stageConfigs.frontendProject}" | md5sum | cut -d ' '  -f 1).tar"
        [[ -f "/home/.cache/node_modules/\$frontendProjectHashZip" ]] && tar -xf "/home/.cache/node_modules/\$frontendProjectHashZip"

        hashBeforeInstall="\$(getNodeModulesListHash)"
        npm install
        hashAfterInstall="\$(getNodeModulesListHash)"

        if [[ \$hashBeforeInstall != \$hashAfterInstall ]]
        then 
            tar -cf \$frontendProjectHashZip node_modules
            rm -f "/home/.cache/node_modules/\$frontendProjectHashZip"
            mv \$frontendProjectHashZip "/home/.cache/node_modules/\$frontendProjectHashZip"
        fi
    """
}

return this