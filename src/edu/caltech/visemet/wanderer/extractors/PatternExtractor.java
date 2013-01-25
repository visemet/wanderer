package edu.caltech.visemet.wanderer.extractors;

import com.eaio.stringsearch.BoyerMooreHorspool;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import edu.caltech.visemet.wanderer.BodyExtractor;
import edu.caltech.visemet.wanderer.Centipede;
import edu.caltech.visemet.wanderer.ResourceFactory;
import edu.caltech.visemet.wanderer.ResourceWrapper;

/**
 *
 * @author Max Hirschhorn #visemet
 */
public class PatternExtractor<R extends ResourceWrapper>
implements BodyExtractor<R> {

    @XStreamAlias("resource-factory")
    private ResourceFactory<R> resourceFactory;

    @XStreamAlias("pattern")
    private String pattern = "";

    private transient BoyerMooreHorspool boyerMooreHorspool;

    public PatternExtractor() { }

    @Override
    public ResourceFactory<R> getResourceFactory() {
        return resourceFactory;
    }

    @Override
    public void setResourceFactory(final ResourceFactory<R> resourceFactory) {
        this.resourceFactory = resourceFactory;
    }

    @Override
    public void initialize(final Centipede<R> centipede) {
        boyerMooreHorspool = new BoyerMooreHorspool();
    }

    @Override
    public void extract(R resource, byte[] body) {
        // <a..href="{{href}}"..>

        throw new UnsupportedOperationException("Not supported yet.");
    }
}
