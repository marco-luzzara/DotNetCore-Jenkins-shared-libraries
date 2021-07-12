package utils;

/**
 * separate the service name from the initial directory in an smb path
 * @param path formed by the service name and initial directory like, "serviceName/init1/init2/init3"
 * @return a list of 2 elements, the first one is the service name ("serviceName" in the previous example),
 *      the second one is the initial directory ("init1/init2/init3" in the previous example).
 */
def splitServiceFromDirectory(String path) {
    path.split('/', 2)
}

return this