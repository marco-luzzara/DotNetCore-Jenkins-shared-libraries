package utils;

import static utils.script.ScriptAdapter.*;

class FileUtils implements Serializable {
    static String[] getLinuxFilesOnRegex(String relativePath, String regex) {
        def files = shReturnStdOut("find ${relativePath} -regextype sed -regex '${regex}'")
            .trim().split('\n');

        files = files.removeAll([""]);

        return files;
    }

    static void deleteLinuxFile(String path) {
        basicSh("rm ${path} || true");
    }
}