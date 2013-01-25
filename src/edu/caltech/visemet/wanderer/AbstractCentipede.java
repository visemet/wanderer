package edu.caltech.visemet.wanderer;

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.HttpResponseBodyPart;
import com.ning.http.client.HttpResponseHeaders;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Response;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @param <R> the type of resources handled by the centipede
 *
 * @author Max Hirschhorn #visemet
 */
@XStreamAlias("centipede")
public abstract class AbstractCentipede<R extends ResourceWrapper>
implements Centipede<R> {

    @XStreamAlias("resource-sieves")
    private final List<ResourceSieve<R>> resourceSieves = new ArrayList<>();

    @XStreamAlias("body-extractors")
    private final List<BodyExtractor<R>> bodyExtractors = new ArrayList<>();

    protected transient Queue<R> resources;

    private transient AsyncHttpClient asyncHttpClient;

    private transient ExecutorService executorService;

    /**
     * Class constructor.
     */
    public AbstractCentipede() { }

    @Override
    public void initialize() {
        resources = new ConcurrentLinkedQueue<>();
        asyncHttpClient = new AsyncHttpClient(new AsyncHttpClientConfig.Builder()
                .setAllowPoolingConnection(true)
                .setFollowRedirects(true)
                .build());

        executorService = Executors.newCachedThreadPool();
        // executorService = Executors.newSingleThreadExecutor();

        for (ResourceSieve<R> resourceSieve : resourceSieves) {
            resourceSieve.initialize();
        }

        for (BodyExtractor<R> bodyExtractor : bodyExtractors) {
            bodyExtractor.initialize(this);
        }
    }

    @Override
    public boolean examine(R resource) {
        synchronized (this) {
            for (ResourceSieve<R> resourceSieve : resourceSieves) {
                if (!resourceSieve.shouldInclude(resource)) {
                    return false;
                }
            }

            notify();
        }

        return resources.add(resource);
    }

    @Override
    public List<ResourceSieve<R>> retrieveResourceSieves() {
        return Collections.unmodifiableList(resourceSieves);
    }

    @Override
    public void apply(ResourceSieve<R> resourceSieve) {
        resourceSieves.add(resourceSieve);
    }

    @Override
    public List<BodyExtractor<R>> retrieveBodyExtractors() {
        return Collections.unmodifiableList(bodyExtractors);
    }

    @Override
    public void apply(BodyExtractor<R> bodyExtractor) {
        bodyExtractor.initialize(this);
        bodyExtractors.add(bodyExtractor);
    }

    @Override
    public synchronized void execute() throws InterruptedException, IOException {
        R iterResource;
        while ((iterResource = resources.poll()) == null) {
            wait();
        }

        final R resource = iterResource;

        try {
            String URI = new URI(resource.getResource()).toString();

            final BoundRequestBuilder request =
                    // asyncHttpClient.prepareGet(resource.getResource());
                    asyncHttpClient.prepareGet(URI);

            final ListenableFuture<byte[]> future =
                    request.execute(new ByteArrayAsyncCompletionHandler());

            for (final BodyExtractor<R> bodyExtractor : bodyExtractors) {
                future.addListener(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            bodyExtractor.extract(resource, future.get());
                        } catch (ExecutionException | InterruptedException ex) {

                        }
                    }
                }, executorService);
            }
        } catch (URISyntaxException ex) {

        }
    }

    @Override
    public void suspend() {
        asyncHttpClient.close();
        executorService.shutdown();
    }

    private static class ByteArrayAsyncCompletionHandler
    extends AsyncCompletionHandler<byte[]> {

        private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        @Override
        public STATE onHeadersReceived(HttpResponseHeaders headers) throws Exception {
            if (headers.getHeaders().getFirstValue("Content-Type").startsWith("text/html")) {
                return super.onHeadersReceived(headers);
            }

            return STATE.ABORT;
        }

        @Override
        public AsyncHandler.STATE onBodyPartReceived(HttpResponseBodyPart content)
                throws Exception {

            bytes.write(content.getBodyPartBytes());
            return AsyncHandler.STATE.CONTINUE;
        }

        @Override
        public byte[] onCompleted(Response response) throws Exception {
            return bytes.toByteArray();
        }
    }
}
