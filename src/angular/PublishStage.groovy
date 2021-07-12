package angular;

def run(Map stageConfigs) {
    sh """
        cd ${stageConfigs.frontendProject}
        ng build --${stageConfigs.environment}
    """
}

return this