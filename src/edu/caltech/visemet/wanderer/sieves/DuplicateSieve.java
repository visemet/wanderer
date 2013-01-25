package edu.caltech.visemet.wanderer.sieves;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import edu.caltech.visemet.wanderer.ResourceSieve;
import edu.caltech.visemet.wanderer.ResourceWrapper;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @param <R> the type of resources processed by the duplicate sieve
 *
 * @author Max Hirschhorn #visemet
 */
@XStreamAlias("duplicate-sieve")
public class DuplicateSieve<R extends ResourceWrapper>
implements ResourceSieve<R> {

    private transient Set<R> resources;

    /**
     * Class constructor.
     */
    public DuplicateSieve() { }

    @Override
    public void initialize() {
        resources = new HashSet<>();
    }

    @Override
    public boolean shouldInclude(R resource) {
        boolean result = false;

        if (!resources.contains(resource)) {
            // result = resources.add(resource);
            resources.add(resource);
            result = true;
        }

        return result;

        // return resources.add(resource);
    }
}
