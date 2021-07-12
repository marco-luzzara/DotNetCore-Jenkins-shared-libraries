package dotnetcore.version3;

def run(Map stageConfigs) {
    sh """#!/bin/bash -xe
        dotnet nuget add source ${stageConfigs.nugetInternalServerUrl}/nuget \
            --name $NUGET_SOURCE_NAME \
            --username $JENKINS_CREDENTIALS_USR \
            --store-password-in-clear-text \
            --password $JENKINS_CREDENTIALS_PSW

        function getPackageListHash {
            tree -L 2 ~/.nuget/packages | md5sum | cut -d ' ' -f 1
        }

        slnHashZip="\$(echo "$SOLUTION_NAME" | md5sum | cut -d ' '  -f 1).tar"
        [[ -f "/home/.cache/nuget/\$slnHashZip" ]] && tar -xf "/home/.cache/nuget/\$slnHashZip" -C ~/.nuget

        hashBeforeInstall="\$(getPackageListHash)"
        dotnet restore
        hashAfterInstall="\$(getPackageListHash)"

        if [[ \$hashBeforeInstall != \$hashAfterInstall ]]
        then 
            (cd ~/.nuget && tar -cf \$slnHashZip packages)
            rm -f "/home/.cache/nuget/\$slnHashZip"
            mv ~/.nuget/\$slnHashZip "/home/.cache/nuget/\$slnHashZip"
        fi

        dotnet build -c Release --no-restore
    """
}

return this