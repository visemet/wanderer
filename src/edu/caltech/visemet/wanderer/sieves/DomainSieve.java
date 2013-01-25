package edu.caltech.visemet.wanderer.sieves;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import edu.caltech.visemet.wanderer.ResourceSieve;
import edu.caltech.visemet.wanderer.ResourceWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Max Hirschhorn #visemet
 */
@XStreamAlias("domain-sieve")
public class DomainSieve<R extends ResourceWrapper>
implements ResourceSieve<R> {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(DomainSieve.class);

    private static final String PREFIX = "http://";

    @XStreamAlias("pattern")
    private String pattern = "";

    @Override
    public void initialize() { }

    @Override
    public boolean shouldInclude(R resource) {
        if (resource == null) {
            return false;
        }

        if (this.pattern.startsWith("**")) {
            return resource.getResource().contains(this.pattern.substring(2));
        } else {
            return resource.getResource().startsWith(PREFIX + this.pattern);
        }
    }
}
