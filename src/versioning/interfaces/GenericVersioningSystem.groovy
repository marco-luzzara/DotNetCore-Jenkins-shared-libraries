package versioning.interfaces;

abstract class GenericVersioningSystem implements Serializable {
    GenericVersioningSystem(Map configs = [:]) {}

    abstract def getLastVersion(Map params = [:]);
    abstract def getNextVersion(Map params = [:]);
    abstract def getLastAndNextVersion(Map params = [:]);
    abstract def publishNewVersion(Map params = [:]);
    abstract def deleteVersion(Map params = [:]);
}