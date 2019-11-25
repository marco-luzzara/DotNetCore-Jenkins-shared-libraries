package utils.script;

class ScriptAdapter implements Serializable {
    static Script script;
    static ScriptUtils adaptee;

    static {
        adaptee = new ScriptUtils();
    }

    static void basicSh(String cmd) {
        adaptee.basicSh(cmd);
    }

    static String shReturnStdOut(String cmd) {
        return adaptee.shReturnStdOut(cmd);
    }

    static int shReturnStatus(String cmd) {
        return adaptee.shReturnStatus(cmd);
    }

    static void log(String msg) {
        script.echo(msg);
    }
}