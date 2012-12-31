package edu.caltech.visemet.wanderer;

/**
 *
 * @param <R> the type of resources made by the factory
 *
 * @author Max Hirschhorn #visemet
 */
public interface ResourceFactory<R extends ResourceWrapper> {

    /**
     * Creates a resource using the specified base resource to represent the
     * specified path.
     *
     * @param baseResource the base resource
     * @param path the path
     *
     * @return a resource instance that represents the specified path with
     * respect to the specified base resource
     */
    R create(R baseResource, String path);
}
