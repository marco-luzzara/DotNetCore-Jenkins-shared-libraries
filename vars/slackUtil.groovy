package globalUtils.notifications;

def getHEADCommitAuthor() {
    return sh(script:'git show --quiet HEAD --format="%an (%ae)"', returnStdout: true).trim();
}

def getGenericNotifyMessage(
    String status,
    String additionalInfo = '') 
{
    def headCommitAuthor = getHEADCommitAuthor();

    return """
        Pipeline: ${env.JOB_NAME}
        Build: #${env.BUILD_NUMBER} on ${env.NODE_NAME}
        Commit of: ${headCommitAuthor}
        Status: ${status}
        Url: ${env.BUILD_URL}
        Additional Info: ${additionalInfo}
        """
}

def notifyBuildStarted() {
    return slackSend(
        color: '', 
        message: getGenericNotifyMessage('Started')); 
}

def notifyBuildSucceeded() {
    return slackSend(
        color: 'good', 
        message: getGenericNotifyMessage('Success')); 
}

def notifyBuildFailed(String excMessage) {
    return slackSend(
        color: '#ff0000', 
        message: getGenericNotifyMessage('Failed', "Exception: ${excMessage}")); 
}

def notifyBuildAborted() {
    return slackSend(
        color: '#ff0000', 
        message: getGenericNotifyMessage('Aborted', 'This build has been ABORTED')); 
}