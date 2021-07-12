package versioning;

def getNextRelease(Map configs) {
    sh(script: '''#!/bin/bash -xe
        git fetch --tags -q
        oldRelease=$(git describe --abbrev=0 --match="[0-9]*.[0-9]*.[0-9]*" 2> /dev/null || echo "0.0.0")

        if [[ "$oldRelease" = "0.0.0" ]]
        then
            echo "1.0.0"
        else
            IFS='.' read -a versionTokens <<< "$oldRelease"
            cmtMsg="$(git log -1 --format="%s" HEAD)"
            messageRegex="^rif\\. #[0-9]+:([a-zA-Z]+):.*"
            releaseType=$([[ "$cmtMsg" =~ $messageRegex ]] ; echo ${BASH_REMATCH[1]})

            case "$releaseType" in
                "major")
                    echo "$((${versionTokens[0]} + 1)).0.0"
                    ;;
                "minor")
                    echo "${versionTokens[0]}.$((${versionTokens[1]} + 1)).0"
                    ;;
                "patch")
                    echo "${versionTokens[0]}.${versionTokens[1]}.$((${versionTokens[2]} + 1))"
                    ;;
                *)
                    echo "release type should be major|minor|patch. \\"$releaseType\\" is not supported"
                    exit 1
                    ;;
            esac
        fi
    ''', returnStdout: true).trim()
}

def confirmNextRelease(Map configs) {
    sh """
        git tag -am "${configs.nextRelease}" "${configs.nextRelease}" HEAD
        git push origin "${configs.nextRelease}"
    """
}

def rollbackNextRelease(Map configs) {
    try {
        sh """
            git push --delete origin "${configs.nextRelease}"
        """
    }
    catch (e) {
        notifyWarning(version: configs.notifier, message: e.message, 
            additionalInfo: e.stackTrace.join('\n'), slackResponse: configs.slackResponse)
    }
}

return this