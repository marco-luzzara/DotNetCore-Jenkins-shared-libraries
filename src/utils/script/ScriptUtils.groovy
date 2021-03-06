package utils.script;

def basicSh(String cmd) {
    sh "${cmd}";
}

def shReturnStdOut(String cmd) {
    return sh(script:"${cmd}", returnStdout: true);
}

def shReturnStatus(String cmd) {
    return sh(script:"${cmd}", returnStatus: true);
}

def log(String msg) {
    echo(msg);
}

return this;