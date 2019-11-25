package globalUtils.notifications;

def getGenericNotifyMessage(
    String jobName, 
    String buildNumber,
    String status, 
    String url,
    String additionalInfo = '') 
{
    return """
        Pipeline: ${jobName}
        Build: #${buildNumber}
        Status: ${status}
        Url: ${url}
        Additional Info: ${additionalInfo}
        """
}

def notifyBuildStarted() {
    slackSend color: '', 
        message: getGenericNotifyMessage(env.JOB_NAME, env.BUILD_NUMBER, 'Started', env.BUILD_URL); 
}

def notifyBuildSucceeded() {
    slackSend color: 'good', 
        message: getGenericNotifyMessage(env.JOB_NAME, env.BUILD_NUMBER, 'Success', env.BUILD_URL); 
}

def notifyBuildFailed(String excMessage) {
    slackSend color: '#ff0000', 
        message: getGenericNotifyMessage(env.JOB_NAME, env.BUILD_NUMBER, 'Failed', 
        env.BUILD_URL, "Exception: " + excMessage); 
}