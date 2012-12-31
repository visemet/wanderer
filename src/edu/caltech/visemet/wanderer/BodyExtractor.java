package edu.caltech.visemet.wanderer;

/**
 *
 * @param <R> the type of resources analyzed by the body extractor
 *
 * @author Max Hirschhorn #visemet
 */
public interface BodyExtractor<R extends ResourceWrapper> {

    /**
     * Returns the resource factory used by this body extractor.
     *
     * @return the resource factory used by this body extractor
     */
    ResourceFactory<R> getResourceFactory();

    /**
     * Replaces the resource factory used by this body extractor with the
     * specified resource factory.
     *
     * @param resourceFactory the resource factory to use
     */
    void setResourceFactory(ResourceFactory<R> resourceFactory);

    /**
     * Initializes this body extractor.
     *
     * @param centipede the centipede with which to interact
     */
    void initialize(Centipede<R> centipede);

    /**
     * Extracts information from the specified body of the specified resource.
     *
     * @param resource the resource from where the body comes
     * @param body the body from which to extract
     */
    void extract(R resource, byte[] body);
}
