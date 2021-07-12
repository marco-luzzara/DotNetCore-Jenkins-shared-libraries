package common;

def run(Map stageConfigs) {
    sh '''#!/bin/bash -xe
        echo :$JENKINS_CREDENTIALS > /etc/gss/mech.d/credentials
        git config --global user.email "$JENKINS_MAIL"
        git config --global user.name "$JENKINS_CREDENTIALS_USR"

        # https://stackoverflow.com/questions/68344358/git-askpass-with-user-and-password/68358639#68358639
        # user core.askPass because env variable does not persist after script shell is closed
        git_askPass_script=$(mktemp) && chmod a+rx $git_askPass_script
        cat > $git_askPass_script <<< '#!/bin/bash -xe
            case "$1" in
                Username*) exec echo "$JENKINS_CREDENTIALS_USR" ;;
                Password*) exec echo "$JENKINS_CREDENTIALS_PSW" ;;
            esac
        '
        git config core.askPass "$git_askPass_script"
    '''
}

return this