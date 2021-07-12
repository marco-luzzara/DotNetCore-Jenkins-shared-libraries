package angular;

def run(Map stageConfigs) {
    sh """
        cd ${stageConfigs.frontendProject}
        ng test --watch=false --browsers=ChromeHeadlessNoSandbox
        # the problem with cypress is that you first need to run the service but, more
        # importantly, the cypress binary should be cached because otherwise
        # it is not installed with npm install and the pipeline throws with something like
        # https://stackoverflow.com/questions/61885551/jenkins-giving-the-cypress-npm-package-is-installed-but-the-cypress-binary-is
        # 
        # the binary to cache is called ~/.cache/Cypress/3.1.5/Cypress/Cypress (or something similar)
        # npx cypress run
    """
}

return this