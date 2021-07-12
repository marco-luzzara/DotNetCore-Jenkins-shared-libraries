def call(Map configs) {
    def notifier = null
    switch (configs.version) {
        case "slack":
            notifier = new notifiers.SlackNotifier()
            break
        default:
            throw new RuntimeException("${configs.version} is not an existing notifier")
    }

    notifier.notifyComplete(configs)
}