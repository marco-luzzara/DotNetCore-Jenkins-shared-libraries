package utils;

import static utils.script.ScriptAdapter.*;

class FileUtils implements Serializable {
    static List getLinuxFilesOnRegex(String relativePath, String regex) {
        def files = shReturnStdOut("find ${relativePath} -regextype sed -regex '${regex}'")
            .trim().split('\n') as List;

        files.removeAll([""]);

        return files;
    }

    static void deleteLinuxFileSafely(String path) {
        basicSh("rm ${path} || true");
    }

    static void deleteLinuxFileOrDir(String filePath) {
        def indexOfSlash = filePath.indexOf('/');

        if (indexOfSlash == -1)
            basicSh("rm -r '${filePath}'");
        else
            basicSh("rm -r '${filePath.substring(0, indexOfSlash)}'");
    }
}