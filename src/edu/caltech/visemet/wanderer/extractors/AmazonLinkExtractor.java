package edu.caltech.visemet.wanderer.extractors;

import com.eaio.stringsearch.BoyerMooreHorspool;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import edu.caltech.visemet.wanderer.BodyExtractor;
import edu.caltech.visemet.wanderer.Centipede;
import edu.caltech.visemet.wanderer.ResourceFactory;
import edu.caltech.visemet.wanderer.ResourceWrapper;
import edu.caltech.visemet.wanderer.Resources;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @param <R> the type of resources analyzed by the link extractor
 *
 * @author Max Hirschhorn #visemet
 */
@XStreamAlias("amazon-link-extractor")
public class AmazonLinkExtractor<R extends ResourceWrapper> implements BodyExtractor<R> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AmazonLinkExtractor.class);

    @XStreamAlias("resource-factory")
    private ResourceFactory<R> resourceFactory;

    private transient Centipede<R> centipede;

    private transient BoyerMooreHorspool boyerMooreHorspool;

    /**
     * Class constructor.
     */
    public AmazonLinkExtractor() { }

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
        final byte[] divBeginOpenPattern1 = "<div id=\"purchaseSimsData\"".getBytes();
        final byte[] divBeginOpenPattern2 = "<div id=\"sessionSimsData\"".getBytes();
        final byte[] divBeginClosePattern = ">".getBytes();
        final byte[] divEndPattern = "</div>".getBytes();

        int offsetPos = boyerMooreHorspool.searchBytes(body, divBeginOpenPattern1);

        if (offsetPos != -1) {
            int pos = boyerMooreHorspool.searchBytes(body, offsetPos, divBeginClosePattern);
            int startPos = pos + divBeginClosePattern.length;

            int endPos = boyerMooreHorspool.searchBytes(body, offsetPos, divEndPattern);

            final String[] ids = new String(Arrays.copyOfRange(body, startPos, endPos)).split(",");

            for (String id : ids) {
                String URI = "http://www.amazon.com/_/dp/" + id;
                // LOGGER.debug("{}", URI);
                centipede.examine(resourceFactory.create(resource, URI));
            }
        } /* else {
            LOGGER.info("{}, {}", body.length, resource.getResource());
            LOGGER.info("{}", new String(body));
        } */

        offsetPos = boyerMooreHorspool.searchBytes(body, divBeginOpenPattern2);

        if (offsetPos != -1) {
            int pos = boyerMooreHorspool.searchBytes(body, offsetPos, divBeginClosePattern);
            int startPos = pos + divBeginClosePattern.length;

            int endPos = boyerMooreHorspool.searchBytes(body, offsetPos, divEndPattern);

            final String[] ids = new String(Arrays.copyOfRange(body, startPos, endPos)).split(",");

            for (String id : ids) {
                String URI = "http://www.amazon.com/_/dp/" + id;
                // LOGGER.debug("{}", URI);
                centipede.examine(resourceFactory.create(resource, URI));
            }
        }

        final byte[] spanBeginOpenPattern = "<span id=\"btAsinTitle\"".getBytes();
        final byte[] spanBeginClosePattern = ">".getBytes();
        final byte[] spanEndPattern = "</span>".getBytes();

        offsetPos = boyerMooreHorspool.searchBytes(body, spanBeginOpenPattern);

        if (offsetPos != -1) {
            int pos = boyerMooreHorspool.searchBytes(body, offsetPos, spanBeginClosePattern);
            int startPos = pos + spanBeginClosePattern.length;

            int endPos = boyerMooreHorspool.searchBytes(body, offsetPos, spanEndPattern);

            String title = new String(Arrays.copyOfRange(body, startPos, endPos));

            if (title.contains("Tweety Bird Glitter")) {
                LOGGER.info("{}, {}", title, resource);
            }
        }


        /* while ((iterPos = boyerMooreHorspool.searchBytes(body, iterPos, aOpenPattern)) != -1) {
            iterPos += aOpenPattern.length;

            int maxPos = boyerMooreHorspool.searchBytes(body, iterPos, aClosePattern);

            int startPos = boyerMooreHorspool.searchBytes(body, iterPos, hrefBeginPattern);
            startPos += hrefBeginPattern.length;

            if (startPos < maxPos) {
                int endPos = boyerMooreHorspool.searchBytes(body, startPos, hrefEndPattern);
                final byte[] href = Arrays.copyOfRange(body, startPos, endPos);

                String URI = Resources.normalize(resource.getResource(), new String(href));

                if (URI != null) {
                    boolean matches = URI.matches("^http://www.amazon.com/[^/]+/dp/B[^/]+/.*$");

                    if (matches) {
                        String[] split = URI.split("/");

                        String id = split[5];
                        URI = "http://www.amazon.com/_/dp/" + id;

                        LOGGER.debug("{}", URI);
                    } else {
                        URI = null;
                    }
                }

                if (URI != null) {
                    centipede.examine(
                            resourceFactory.create(resource, URI));
                }
            }
        } */
    }
}
