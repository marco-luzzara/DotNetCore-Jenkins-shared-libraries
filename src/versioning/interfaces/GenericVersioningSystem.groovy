package versioning.interfaces;

abstract class GenericVersioningSystem implements Serializable {
    GenericVersioningSystem(Map configs = [:]) {}

    abstract String getLastVersion();
    abstract String getNextVersion();
}