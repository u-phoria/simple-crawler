package simplecrawler;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

// Improvements:
// - capture richer information per link - content type, title etc
// - better content type handling, right now we just look for html in it
// - more efficient response processing, currently we GET full content for
//   along with checking content type, we chould use HEAD + only retrieve
//   content if we can extract links
// - sitemap built up behind a blocking call + in memory before returning
//   - fine for now but only scales so far, could think about async operation,
//   callbacks, ultimately parallelising + extracting state for sharing etc//
// - we currently depend on the thread pool of whatever implements Fetcher for
//   callback processing, not immediately oobvious, own thred pool would b
//   more so + allow better tuning
// - pluggable / extensible strategies for following links etc
// ...
public class SingleDomainCrawler {
    private static final Logger LOG = LoggerFactory.getLogger(SingleDomainCrawler.class);

    private Fetcher fetcher;
    private HtmlLinkExtractor parser;

    public SingleDomainCrawler(Fetcher fetcher, HtmlLinkExtractor linkExtractor) {
        this.fetcher = fetcher;
        this.parser = linkExtractor;
    }

    public Map<URL, Set<URL>>run(URL domainRoot) {
        final AtomicInteger inProgress = new AtomicInteger(0);
        final BlockingQueue<URL> queue = new LinkedBlockingQueue<>();
        Set<URL> visited = Collections.synchronizedSet(new HashSet<URL>());
        ConcurrentMap<URL, Set<URL>> result = new ConcurrentHashMap<>();

        queue.offer(domainRoot);
        while(!queue.isEmpty() || inProgress.get() > 0) {
            final URL nextUrl = pollQueue(queue);
            if (nextUrl == null)
                continue;

            LOG.debug("Crawl from {}", nextUrl);
            inProgress.incrementAndGet();
            fetcher.fetch(
                nextUrl,
                fetchResult -> {
                    try {
                        if (fetchResult == null)
                            return;
                        visited.add(fetchResult.getUrl());
                        if (!isWebPage(fetchResult.getContentType()))
                            return;
                        result.putIfAbsent(nextUrl, Collections.synchronizedSet(new HashSet<>()));
                        Set<URL> extractedUrls = extractLinksFromFetchedContent(fetchResult, nextUrl);
                        for (URL url : extractedUrls) {
                            result.get(nextUrl).add(url);
                            if (!visited.contains(url) && shouldFollow(url, fetchResult.getContentType(), domainRoot)) {
                                queue.offer(url);
                            }
                        }
                    } catch (Exception e) {
                        LOG.error("Error processing content for {}: {}", nextUrl, e);
                    } finally {
                        inProgress.decrementAndGet();
                    }
                },
                error -> {
                    inProgress.decrementAndGet();
                    LOG.info("Failed to get {}", nextUrl);
                });
        }
        return result;
    }

    private boolean shouldFollow(URL url, String contentType, URL domainRoot) {
        return isInDomain(url, domainRoot) && isWebPage(contentType);
    }

    private Set<URL> extractLinksFromFetchedContent(Fetcher.FetchResult fetchResult, URL parent) {
        if (!isWebPage(fetchResult.getContentType()))
            return Collections.emptySet();
        return parser.extractLinks(fetchResult.getBody(), parent);
    }

    private boolean isWebPage(String contentType) {
        // crude for now, should talk more formally in terms of media types
        return Optional.ofNullable(contentType).orElse("")
                .toLowerCase().contains("html");
    }

    private boolean isInDomain(URL url, URL domainRoot) {
        return url.getHost().contains(domainRoot.getHost());
    }

    private URL pollQueue(BlockingQueue<URL> queue) {
        try {
            return queue.poll(200, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            LOG.error(e.getMessage(), e);
            return null;
        }
    }
}
