package dotnetcore.pipeline.stage;

import static utils.vcs.VCSAdapter.gitCheckout;
import interfaces.pipeline.stage.GenericStage;

class CheckoutStage extends GenericStage {
    String credentialsId
    String repoUrl

    CheckoutStage(Map stageConfigs = [:]) {
        super(stageConfigs);

        this.credentialsId = stageConfigs.credentialsId;
        this.repoUrl = stageConfigs.repoUrl;  
    }

    def run() {
        gitCheckout(this.credentialsId, this.repoUrl);
    }

    @Override
    void rollback() {
    }
}