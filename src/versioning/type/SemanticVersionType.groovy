package versioning.type;

import static utils.script.ScriptAdapter.log;

enum SemanticVersionType {
    MAJOR, MINOR, PATCH

    static String computeNextVersion(String version, SemanticVersionType versionType) {
        def versionNumbers = version.split('\\.');
        int lastMajor = versionNumbers[0].toInteger();
        int lastMinor = versionNumbers[1].toInteger();
        int lastPatch = versionNumbers[2].toInteger();

        def nextVersion = "";
        switch (versionType) {
            case SemanticVersionType.MAJOR: 
                nextVersion = "${lastMajor + 1}.0.0";
                break;
            case SemanticVersionType.MINOR:
                nextVersion = "${lastMajor}.${lastMinor + 1}.0";
                break;
            case SemanticVersionType.PATCH:
                nextVersion = "${lastMajor}.${lastMinor}.${lastPatch + 1}";
                break;
            default:
                throw new RuntimeException("Version type must be one of the following values: Major, Minor, Patch");
        }

        log("nextVersion: ${nextVersion}");

        return nextVersion;
    }
}