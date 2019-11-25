package utils;

class PipelineStages {
    static final String CHECKOUT = "checkout";
    static final String RESTORE = "restore";
    static final String CLEAN = "clean";
    static final String BUILD = "build";
    static final String UNITTEST = "unitTest";
    static final String INTEGRATIONTEST = "integrationTest";
    static final String VERSIONING = "versioning";
    static final String PACK = "pack";
    static final String PUSHPACKAGE = "pushPackage";
    static final String PUBLISHARTIFACT = "publishArtifact";
    static final String COMPLETE = "complete";
}