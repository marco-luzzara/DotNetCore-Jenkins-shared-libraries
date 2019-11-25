package utils;

import java.util.regex.Pattern;

import static utils.script.ScriptAdapter.log;

class RegexUtils implements Serializable {
    static String[] getCGroupsAgainstPattern(String text, Pattern pattern, int group_number) {
        def matcher = text =~ pattern;
        String[] groups = new String[group_number];
        
        try {
            def i = 0;
            while (i < group_number) {
                groups[i] = matcher[0][i + 1]
                i++;
            }
        }
        catch (IndexOutOfBoundsException exc) {
            log("did not match all cgroups for ${pattern} in ${text}");
        }
        finally {
            return groups;
        }
    }
}