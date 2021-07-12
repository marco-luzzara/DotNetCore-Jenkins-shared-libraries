package notifiers;

def notifyStart(Map configs) {
    def channel = configs.channel ?: "idsign-publish-jenkins"

    def userIds = slackUserIdsFromCommitters()
    def userIdsString = userIds.collect { "<@$it>" }.join(' ')
    def previousBuildField = currentBuild.previousSuccessfulBuild == null ?
        "None" :
        "<${currentBuild.previousSuccessfulBuild.absoluteUrl}|${currentBuild.previousSuccessfulBuild.displayName}>"

    blocks = [
        [
            "type": "header",
            "text": [
                "type": "plain_text",
				"text": configs.slnName,
				"emoji": true
            ]
        ],
        [
            "type": "section",
            "fields": [
                [
                    "type": "mrkdwn",
					"text": "*Branch:*\n${configs.branchName}"
                ],
                [
                    "type": "mrkdwn",
					"text": "*Build Url:*\n<${env.BUILD_URL}|Jenkins build>"
                ]
            ]
        ],
        [
            "type": "section",
            "fields": [
                [
                    "type": "mrkdwn",
					"text": "*Authors:*\n${userIdsString}"
                ],
                [
                    "type": "mrkdwn",
					"text": "*Build Number:*\n${env.BUILD_DISPLAY_NAME}"
                ]
            ]
        ],
        [
            "type": "section",
            "fields": [
                [
                    "type": "mrkdwn",
					"text": "*Docker image:*\n${configs.dockerImage}"
                ],
                [
                    "type": "mrkdwn",
					"text": "*Previous Successful build:*\n${previousBuildField}"
                ]
            ]
        ],
        [
            "type": "divider"
        ],
        [
            "type": "actions",
            "elements": [
                [
					"type": "button",
					"text": [
						"type": "plain_text",
						"text": "Click here to see Live Output"
                    ],
					"url": "${env.BUILD_URL}console"
                ]
            ]
        ]
    ]
    slackSend(channel: channel, blocks: blocks)
}

def notifyComplete(Map configs) {
    def color = currentBuild.currentResult == 'SUCCESS' ? 'good' : 'danger'
    def message = currentBuild.currentResult == 'SUCCESS' ? 
        'The build completed successfully :white_check_mark:' :
        "The build failed :x: . Check build steps <${env.BUILD_URL}flowGraphTable|here>"
    slackSend(color: color, channel: configs.slackResponse.threadId, message: message)
}

def notifyWarning(Map configs) {
    def message = "_Warning_\n*${configs.message}*\n\n```${configs.additionalInfo}```"
    slackSend(color: 'warning', channel: configs.slackResponse.threadId, message: message)
}

return this;