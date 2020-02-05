package versioning.interfaces;

abstract class GenericVersioningSystem implements Serializable {
    GenericVersioningSystem(Map configs = [:]) {}

    abstract def getLastVersion(Maps params = [:]);
    abstract def getNextVersion(Maps params = [:]);
    abstract def getLastAndNextVersion(Map params = [:]);
    abstract def publishNewVersion(Map params = [:]);
    abstract def deleteVersion(Maps params = [:]);
}