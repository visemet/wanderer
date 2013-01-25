package edu.caltech.visemet.wanderer.centipedes;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import edu.caltech.visemet.wanderer.AbstractCentipede;
import edu.caltech.visemet.wanderer.ResourceFactory;
import edu.caltech.visemet.wanderer.ResourceSieve;
import edu.caltech.visemet.wanderer.sieves.DuplicateSieve;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Max Hirschhorn #visemet
 */
@XStreamAlias("count-centipede")
public class CountCentipede<R extends CountCentipede.HistogramResource>
extends AbstractCentipede<R> implements ResourceFactory<R> {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(CountCentipede.class);

    @XStreamAlias("max-count")
    private int maxCount;

    private transient int currCount;

    private transient Set<HistogramResource> crawledResources;

    private transient Map<String, HistogramResource> createdResources;

    public CountCentipede() { }

    @Override
    public void initialize() {
        super.initialize();

        resources = new PriorityBlockingQueue<>(1, new Comparator<R>() {

            @Override
            public int compare(R r1, R r2) {
                return r1.getDepth() - r2.getDepth();
            }
        });

        this.currCount = 0;
        this.crawledResources = new HashSet<>();
        // this.crawledResources = new ConcurrentSkipListSet<>();
        // this.createdResources = new HashMap<>();
        this.createdResources = new ConcurrentHashMap<>();
    }

    @Override
    public synchronized boolean shouldTerminate() {
        // this.currCount = this.crawledResources.size();

        // LOGGER.debug("currCount {}", this.currCount);

        final boolean shouldTerminate = this.currCount > this.maxCount;

        if (shouldTerminate) {
            /* synchronized (this) {
                // for (HistogramResource resource : this.createdResources.values()) {
                for (HistogramResource resource : this.crawledResources) {
                    LOGGER.info("{}", resource);
                }
            } */

            /* Iterator<HistogramResource> it = this.crawledResources.iterator();
            while (it.hasNext()) {
                LOGGER.info("{}", it.next());
            } */

            for (HistogramResource resource : this.crawledResources) {
                LOGGER.info("{}", resource);
            }
        }

        return shouldTerminate;
    }

    @Override
    public synchronized R create(R base, String path) {
        HistogramResource resource = this.createdResources.get(path);

        if (resource == null) {
            final int depth = base.getDepth() + 1;

            resource = new HistogramResource(depth, path);

            boolean include = true;
            for (ResourceSieve<R> resourceSieve : retrieveResourceSieves()) {
                if (!(resourceSieve instanceof DuplicateSieve) && !resourceSieve.shouldInclude((R) resource)) {
                    include = false;
                }
            }

            if (include) {
                this.createdResources.put(path, resource);
            } else {
                resource = null;
            }

            // synchronized (this) {
                /* if (!this.crawledResources.contains(base)) {
                    this.crawledResources.add(base);
                    this.currCount++;

                    LOGGER.debug("{}", base);
                } */
            // }

            // shouldTerminate();
        }

        if (!this.crawledResources.contains(base)) {
            this.crawledResources.add(base);
            this.currCount++;

            // LOGGER.debug("{}", base);
        }

        // LOGGER.debug("{}", base

        if (resource != null) {
            base.addOutgoing(resource.getResource());
            resource.addIncoming(base.getResource());
        }

        return (R) resource;
    }

    @XStreamAlias("histogram-resource-factory")
    public static class Factory<R extends HistogramResource>
    implements ResourceFactory<R> {

        private Map<String, HistogramResource> resources;

        public Factory() {
            this.resources = new HashMap<>();
        }

        @Override
        public R create(final HistogramResource previous, final String URI) {
            HistogramResource resource = this.resources.get(URI);

            if (resource == null) {
                final int depth = previous.getDepth() + 1;

                resource = new HistogramResource(depth, URI);
                this.resources.put(URI, resource);
            }

            previous.outgoing.add(resource.getResource());
            resource.incoming.add(previous.getResource());

            LOGGER.debug("{}", previous);

            return (R) resource;
        }
    }

    @XStreamAlias("histogram-resource")
    public static class HistogramResource extends DepthCentipede.DepthResource {
    // implements Comparable<HistogramResource> {

        private final Set<String> incoming;

        private final Set<String> outgoing;

        public HistogramResource(int depth, String resource) {
            super(depth, resource);

            this.incoming = new HashSet<>();
            this.outgoing = new HashSet<>();
        }

        public void addIncoming(String resource) {
            this.incoming.add(resource);
        }

        public void addOutgoing(String resource) {
            this.outgoing.add(resource);
        }

        /* @Override
        public int compareTo(HistogramResource other) {
            return (getDepth() - other.getDepth());
        } */

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();

            sb.append(getResource());

            Iterator<String> it = this.outgoing.iterator();

            sb.append(" [");

            if (it.hasNext()) {
                sb.append(it.next());
            }

            while (it.hasNext()) {
                sb.append(", ").append(it.next());
            }

            sb.append("]");

            return sb.toString();
        }
    }
}
