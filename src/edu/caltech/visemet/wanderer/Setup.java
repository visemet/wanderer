package edu.caltech.visemet.wanderer;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import edu.caltech.visemet.wanderer.centipedes.DepthCentipede;
import edu.caltech.visemet.wanderer.extractors.LinkExtractor;
import edu.caltech.visemet.wanderer.sieves.DuplicateSieve;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Max Hirschhorn #visemet
 */
@XStreamAlias("setup")
public class Setup<R extends ResourceWrapper> {

    /**
     * Defines the logging utility used by the setup.
     */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(Setup.class);

    @XStreamAlias("centipede")
    private Centipede<R> centipede;

    @XStreamAlias("resources")
    private List<R> resources = new ArrayList<>();

    /**
     * Class constructor.
     */
    public Setup() { }

    /**
     * Returns the class of the specified object, or {@code null} if the
     * specified object is {@code null}.
     *
     * @param obj the object
     *
     * @return the class of the specified object, or {@code null} if the
     * specified object is {@code null}
     */
    private static Class<?> getClass(final Object obj) {
        return obj != null ? obj.getClass() : null;
    }

    /**
     * Stores the configuration of the specified setup to the specified
     * output stream.
     *
     * @param <I> the type of individuals in the specified setup
     * @param setup the setup to store
     * @param stream the output stream
     *
     * @throws IOException if an input or output error occurred
     */
    public static <R extends ResourceWrapper> void store(
            final Setup<R> setup, final OutputStream stream) throws IOException {

        XStream xstream = new XStream();
        xstream.processAnnotations(Setup.Loader.class);

        Loader loader = new Loader();
        loader.load(getClass(setup));

        Centipede<R> centipede = setup.getCentipede();
        loader.load(getClass(centipede));

        for (ResourceSieve<R> resourceSieve : centipede.retrieveResourceSieves()) {
            loader.load(getClass(resourceSieve));
        }

        for (BodyExtractor<R> bodyExtractor : centipede.retrieveBodyExtractors()) {
            loader.load(getClass(bodyExtractor));
            loader.load(getClass(bodyExtractor.getResourceFactory()));
        }

        for (Class<?> clazz : loader.asList()) {
            xstream.processAnnotations(clazz);
        }

        try (ObjectOutputStream out = xstream.createObjectOutputStream(stream)) {
            out.writeObject(loader);
            out.writeObject(setup);
        }
    }

    /**
     * Loads a setup based on the configuration from the specified input
     * stream.
     *
     * @param <I> the type of individuals in the setup
     * @param stream the input stream
     *
     * @return a setup configured by the specified input stream
     *
     * @throws IOException if an input or output error occurred
     * @throws ClassNotFoundException if the class was not found
     */
    @SuppressWarnings("unchecked")
    public static <R extends ResourceWrapper> Setup<R> load(
            final InputStream stream) throws IOException, ClassNotFoundException {

        XStream xstream = new XStream();
        xstream.processAnnotations(Setup.Loader.class);

        Setup<R> setup;

        try (ObjectInputStream in = xstream.createObjectInputStream(stream)) {
            Loader loader = (Loader) in.readObject();

            for (Class<?> clazz : loader.asList()) {
                xstream.processAnnotations(clazz);
            }

            setup = (Setup<R>) in.readObject();
        }

        return setup;
    }

    public Centipede<R> getCentipede() {
        return centipede;
    }

    public void setCentipede(final Centipede<R> centipede) {
        this.centipede = centipede;
    }

    public boolean addResource(R resource) {
        return resources.add(resource);
    }

    public List<R> getResources() {
        return Collections.unmodifiableList(resources);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
            throws IOException, ClassNotFoundException, InterruptedException {

        if (args.length == 1) {
            String filename = args[0];

            Setup<ResourceWrapper> setup =
                    Setup.load(new FileInputStream(filename));

            Centipede<ResourceWrapper> centipede = setup.getCentipede();
            centipede.initialize();

            for (ResourceWrapper resource : setup.getResources()) {
                centipede.examine(resource);
            }

            while (!centipede.shouldTerminate()) {
                centipede.execute();
            }

            centipede.suspend();
        } else {
            Setup<ResourceWrapper> setup = new Setup<>();

            setup.addResource(new DepthCentipede.DepthResource(
                    0, "http://localhost:3000"));

            Centipede<ResourceWrapper> centipede = new DepthCentipede();
            centipede.initialize();

            for (ResourceWrapper resource : setup.getResources()) {
                centipede.examine(resource);
            }

            DuplicateSieve<ResourceWrapper> duplicateSieve =
                    new DuplicateSieve<>();

            centipede.apply(duplicateSieve);

            LinkExtractor<ResourceWrapper> linkExtractor = new LinkExtractor<>();
            linkExtractor.setResourceFactory(new DepthCentipede.Factory());

            centipede.apply(linkExtractor);

            setup.setCentipede(centipede);

            Setup.store(setup, new FileOutputStream("setup.xml"));
        }
    }

    /**
     * Container to properly unmarshal a setup when annotations are used.
     */
    @XStreamAlias("loader")
    private static class Loader {

        /**
         * Holds the list of classes of this loader.
         */
        @XStreamImplicit(itemFieldName="class")
        private final Set<Class<?>> classes = new LinkedHashSet<>();

        /**
         * Class constructor.
         */
        private Loader() { }

        /**
         * Adds the specified class to the list of classes of this loader.
         *
         * @param clazz the class to load
         */
        public void load(final Class<?> clazz) {
            if (clazz != null) {
                classes.add(clazz);
            }
        }

        /**
         * Returns this loader as a list.
         *
         * @return the list of classes of this loader
         */
        public List<Class<?>> asList() {
            return Collections.<Class<?>>unmodifiableList(
                    new ArrayList<>(classes));
        }
    }
}
