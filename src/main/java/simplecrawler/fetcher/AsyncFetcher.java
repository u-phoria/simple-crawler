package simplecrawler.fetcher;

import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import simplecrawler.Fetcher;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.util.function.Consumer;

// Improvements:
// - configurable timeouts
// - do head
// - limit number of concurrent connections so we don't look like a DOS attack
// ...
public class AsyncFetcher implements Fetcher, Closeable {
    private static final Logger LOG = LoggerFactory.getLogger(AsyncFetcher.class);
    public static final int MAX_CONCURRENT = 20;
    private CloseableHttpAsyncClient httpclient;

    public AsyncFetcher() {
        initHttpClient();
    }

    @Override
    public void fetch(URL url, Consumer<FetchResult> resultConsumer, Consumer<Exception> exceptionConsumer) {
        final HttpGet request = new HttpGet(url.toExternalForm());

        httpclient.execute(request, new FutureCallback<HttpResponse>() {

            public void completed(final HttpResponse response) {
                LOG.debug(request.getRequestLine() + "->" + response.getStatusLine());
                String content;
                try {
                    content = EntityUtils.toString(response.getEntity());
                    resultConsumer.accept(new FetchResult(url, response.getEntity().getContentType().getValue().trim(), content));
                } catch (IOException e) {
                    LOG.error("Failed to get content for " + request.getRequestLine(), e);
                }
            }

            public void failed(final Exception ex) {
                LOG.error(request.getRequestLine() + "->" + ex);
                exceptionConsumer.accept(ex);
            }

            public void cancelled() {
                LOG.warn(request.getRequestLine() + " unexpectedly cancelled");
            }
        });
    }

    private void initHttpClient() {
        try {
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(10000).setSocketTimeout(10000).build();
            PoolingNHttpClientConnectionManager poolingConnManager = new PoolingNHttpClientConnectionManager(new DefaultConnectingIOReactor());
            poolingConnManager.setMaxTotal(MAX_CONCURRENT);
            poolingConnManager.setDefaultMaxPerRoute(MAX_CONCURRENT);
            httpclient = HttpAsyncClients.custom()
                    .setConnectionManager(poolingConnManager)
                    .setDefaultRequestConfig(requestConfig).build();
            httpclient.start();
        } catch (Exception e) {
            LOG.error("Error initialising http client", e);
        }
    }

    @Override
    public void close() {
        try {
            httpclient.close();
        } catch (IOException e) {
            LOG.debug("Exception while closing http client", e);
        }
    }
}
