package edu.caltech.visemet.wanderer;

/**
 *
 * @param <R> the type of resources processed by the sieve
 *
 * @author Max Hirschhorn #visemet
 */
public interface ResourceSieve<R extends ResourceWrapper> {

    /**
     * Initializes this resource sieve.
     */
    void initialize();

    /**
     * Return whether or not the specified resource should be examined.
     *
     * @param resource the resource to potentially examine
     *
     * @return {@code true} if this resource should be included, and
     * {@code false} otherwise
     */
    boolean shouldInclude(R resource);
}
