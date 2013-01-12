package edu.caltech.visemet.wanderer;

import java.io.IOException;
import java.util.List;

/**
 *
 * @param <R> the type of resources handled by the centipede
 *
 * @author Max Hirschhorn #visemet
 */
public interface Centipede<R extends ResourceWrapper> {

    /**
     * Initializes this centipede.
     */
    void initialize();

    /**
     * Adds the specified resource to the queue of resources that are examined.
     *
     * @param resource the resource to examine
     *
     * @return
     */
    boolean examine(R resource);

    /**
     * Returns the list of resource sieves used by this centipede.
     *
     * @return the list of resource sieves used by this centipede
     */
    List<ResourceSieve<R>> retrieveResourceSieves();

    /**
     * Adds the specified resource sieve to the list of resource sieves used by
     * this centipede.
     *
     * @param resourceSieve the resource sieve to apply
     */
    void apply(ResourceSieve<R> resourceSieve);

    /**
     * Returns the list of body extractors used by this centipede.
     *
     * @return the list of body extractors used by this centipede
     */
    List<BodyExtractor<R>> retrieveBodyExtractors();

    /**
     * Adds the specified body extractor to the list of body extractors used by
     * this centipede.
     *
     * @param bodyExtractor the body extractor to apply
     */
    void apply(BodyExtractor<R> bodyExtractor);

    /**
     * Executes this centipede.
     *
     * @throws InterruptedException if an interrupted exception occurs
     * @throws IOException if an input or output exception occurs
     */
    void execute() throws InterruptedException, IOException;

    /**
     * Suspends this centipede.
     */
    void suspend();

    /**
     * Returns whether or not this centipede should continue to execute.
     *
     * @return {@code true} if this centipede should terminate, and
     * {@code false} otherwise
     */
    boolean shouldTerminate();
}
