package edu.caltech.visemet.wanderer.centipedes;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import edu.caltech.visemet.wanderer.AbstractCentipede;
import edu.caltech.visemet.wanderer.ResourceFactory;
import edu.caltech.visemet.wanderer.ResourceWrapper;
import edu.caltech.visemet.wanderer.centipedes.DepthCentipede.DepthResource;
import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.PriorityBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Max Hirschhorn #visemet
 */
@XStreamAlias("depth-centipede")
public class DepthCentipede<R extends DepthResource>
extends AbstractCentipede<R> {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(DepthCentipede.class);

    @XStreamAlias("max-depth")
    private int maxDepth = 10;

    private transient int currDepth;

    /**
     * Class constructor.
     */
    public DepthCentipede() { }

    @Override
    public void initialize() {
        super.initialize();

        resources = new PriorityBlockingQueue<>(1, new Comparator<R>() {

            @Override
            public int compare(R r1, R r2) {
                return r2.getDepth() - r1.getDepth();
            }
        });
        currDepth = 0;
    }

    @Override
    public boolean examine(R resource) {
        boolean result = super.examine(resource);

        if (result) {
            LOGGER.debug("{}", resource);

            int depth = resource.getDepth();

            if (currDepth < depth) {
                currDepth = depth;
            }
        }

        return result;
    }

    @Override
    public boolean shouldTerminate() {
        return (currDepth > maxDepth);
    }

    @XStreamAlias("depth-resource-factory")
    public static class Factory<R extends DepthResource>
    implements ResourceFactory<R> {

        public Factory() { }

        @Override
        public R create(
                final DepthResource resource, final String URI) {

            final int depth = resource.getDepth() + 1;
            return (R) new DepthResource(depth, URI);
        }
    }

    @XStreamAlias("depth-resource")
    public static class DepthResource implements ResourceWrapper {

        private final int depth;

        private final String resource;

        public DepthResource(int depth, String resource) {
            this.depth = depth;
            this.resource = resource;
        }

        public int getDepth() {
            return depth;
        }

        @Override
        public String getResource() {
            return resource;
        }

        @Override
        public int hashCode() {
            int hash = 7;

            hash = 41 * hash + Objects.hashCode(this.resource);

            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            } else if (getClass() != obj.getClass()) {
                return false;
            }

            final DepthResource other = (DepthResource) obj;

            if (!Objects.equals(this.resource, other.resource)) {
                return false;
            }

            return true;
        }

        @Override
        public String toString() {
            return resource;
        }
    }
}
