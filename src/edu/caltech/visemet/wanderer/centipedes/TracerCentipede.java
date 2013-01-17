package edu.caltech.visemet.wanderer.centipedes;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import edu.caltech.visemet.wanderer.AbstractCentipede;
import edu.caltech.visemet.wanderer.ResourceFactory;
import edu.caltech.visemet.wanderer.centipedes.TracerCentipede.HistoryResource;
import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Max Hirschhorn #visemet
 */
@XStreamAlias("tracer-centipede")
public class TracerCentipede<R extends HistoryResource>
extends AbstractCentipede<R> {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(TracerCentipede.class);

    @XStreamAlias("target")
    private String target = "";

    private transient boolean foundTarget;

    public TracerCentipede() { }

    @Override
    public void initialize() {
        super.initialize();

        resources = new PriorityBlockingQueue<>(1, new Comparator<R>() {

            @Override
            public int compare(R r1, R r2) {
                return r1.getDepth() - r2.getDepth();
            }
        });

        this.foundTarget = false;
    }

    @Override
    public boolean examine(R resource) {
        boolean result = super.examine(resource);

        if (result) {
            LOGGER.debug("({}) {}", resource.getDepth(), resource);
        }

        if (resource.getResource().equals(target)) {
            LOGGER.info("({}) {}", resource.getDepth(), resource);
            this.foundTarget = true;
        }

        // System.gc();

        return result;
    }

    @Override
    public boolean shouldTerminate() {
        return foundTarget;
    }

    @XStreamAlias("history-resource-factory")
    public static class Factory<R extends HistoryResource>
    implements ResourceFactory<R> {

        public Factory() { }

        @Override
        public R create(final HistoryResource resource, final String URI) {

            final int depth = resource.getDepth() + 1;
            return (R) new HistoryResource(depth, URI, resource);
        }
    }

    @XStreamAlias("history-resource")
    public static class HistoryResource extends DepthCentipede.DepthResource {

        private HistoryResource previous;

        public HistoryResource(int depth, String resource, HistoryResource previous) {
            super(depth, resource);
            this.previous = previous;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();

            sb.append(getResource());

            HistoryResource resource = this;
            while (resource.previous != null) {
                resource = resource.previous;
                sb.append(" <- ").append(resource.getResource());
            }

            return sb.toString();
        }
    }
}
