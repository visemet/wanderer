package edu.caltech.visemet.wanderer.extractors;

import com.eaio.stringsearch.BoyerMooreHorspool;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import edu.caltech.visemet.wanderer.BodyExtractor;
import edu.caltech.visemet.wanderer.Centipede;
import edu.caltech.visemet.wanderer.ResourceFactory;
import edu.caltech.visemet.wanderer.ResourceWrapper;
import edu.caltech.visemet.wanderer.Resources;
import java.util.Arrays;

/**
 *
 * @param <R> the type of resources analyzed by the link extractor
 *
 * @author Max Hirschhorn #visemet
 */
@XStreamAlias("link-extractor")
public class LinkExtractor<R extends ResourceWrapper> implements BodyExtractor<R> {

    @XStreamAlias("resource-factory")
    private ResourceFactory<R> resourceFactory;

    private transient Centipede<R> centipede;

    private transient BoyerMooreHorspool boyerMooreHorspool;

    /**
     * Class constructor.
     */
    public LinkExtractor() { }

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
        this.centipede = centipede;

        boyerMooreHorspool = new BoyerMooreHorspool();
    }

    @Override
    public void extract(final R resource, final byte[] body) {
        int iterPos = 0;

        // <a..href="{{href}}"..>
        final byte[] aOpenPattern = "<a".getBytes();
        final byte[] hrefBeginPattern = "href=\"".getBytes();
        final byte[] hrefEndPattern = "\"".getBytes();
        final byte[] aClosePattern = ">".getBytes();

        while ((iterPos = boyerMooreHorspool.searchBytes(body, iterPos, aOpenPattern)) != -1) {
            iterPos += aOpenPattern.length;

            int maxPos = boyerMooreHorspool.searchBytes(body, iterPos, aClosePattern);

            int startPos = boyerMooreHorspool.searchBytes(body, iterPos, hrefBeginPattern);
            startPos += hrefBeginPattern.length;

            if (startPos < maxPos) {
                int endPos = boyerMooreHorspool.searchBytes(body, startPos, hrefEndPattern);

                if (endPos != -1) {
                    final byte[] href = Arrays.copyOfRange(body, startPos, endPos);

                    final String URI = Resources.normalize(
                            resource.getResource(), new String(href).trim());

                    if (URI != null) {
                        centipede.examine(
                                resourceFactory.create(resource, URI));
                    }
                }
            }
        }
    }
}
