package simplecrawler;

import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.util.*;
import java.util.function.Consumer;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class SingleDomainCrawlerTest {
    private URL DOMAIN_ROOT;
    private URL PNG_URL;
    private URL SUBDOMAIN_URL;
    private URL EXTERNAL_URL;
    private Fetcher fakeFetcher;
    private HtmlLinkExtractor fakeLinkExtractor;
    private LinkedList<Fetcher.FetchResult> fetchResults = new LinkedList<>();
    private LinkedList<Set<URL>> extractorResults = new LinkedList<>();
    private SingleDomainCrawler crawler;

    @Before
    public void before() throws Exception {
        DOMAIN_ROOT = new URL("http://moo.com");
        PNG_URL = new URL("http://moo.com/a.png");
        SUBDOMAIN_URL = new URL("http://subdomain.moo.com/b");
        EXTERNAL_URL = new URL("http://external.domain.com/");

        fakeFetcher = new Fetcher() {
            @Override public void fetch(URL url, Consumer<FetchResult> resultConsumer, Consumer<Exception> exceptionConsumer) {
                resultConsumer.accept(fetchResults.poll());
            }
        };
        fakeLinkExtractor = new HtmlLinkExtractor() {
            @Override public Set<URL> extractLinks(String html, URL parent) {
                return Optional.ofNullable(extractorResults.poll()).orElse(Collections.emptySet());
            }
        };
        crawler = new SingleDomainCrawler(fakeFetcher, fakeLinkExtractor);
    }

    @Test
    public void noDataForNoRootContent() throws Exception {
        Map<URL, Set<URL>> res = crawler.run(DOMAIN_ROOT);
        assertTrue(res.isEmpty());
    }

    @Test
    public void emptyRootContent() throws Exception {
        fetchResults.add(new Fetcher.FetchResult(DOMAIN_ROOT, "text/html", ""));
        Map<URL, Set<URL>> res = crawler.run(DOMAIN_ROOT);
        assertTrue(res.get(DOMAIN_ROOT).isEmpty());
    }

    @Test
    public void twoChildren() throws Exception {
        fetchResults.add(new Fetcher.FetchResult(DOMAIN_ROOT, "text/html", "body"));
        extractorResults.add(new HashSet<URL>() {{
            add(new URL("http://moo.com/a"));
            add(new URL("http://moo.com/b"));
        }});

        Map<URL, Set<URL>> res = crawler.run(DOMAIN_ROOT);

        assertThat(res.get(DOMAIN_ROOT), equalTo(new HashSet<URL>() {{
            add(new URL("http://moo.com/a"));
            add(new URL("http://moo.com/b"));
        }}));
    }

    @Test
    public void twoSitesTwoLevelsWithSubdomainAndExternal() throws Exception {
        fetchResults.add(new Fetcher.FetchResult(DOMAIN_ROOT, "text/html", "body"));
        fetchResults.add(new Fetcher.FetchResult(PNG_URL, "image/png", "image"));
        fetchResults.add(new Fetcher.FetchResult(SUBDOMAIN_URL, "text/html", "body"));

        extractorResults.add(new HashSet<URL>(Arrays.asList( PNG_URL, SUBDOMAIN_URL)));
        extractorResults.add(new HashSet<URL>(Arrays.asList(EXTERNAL_URL)));

        Map<URL, Set<URL>> res = crawler.run(DOMAIN_ROOT);

        assertThat(res.get(DOMAIN_ROOT), equalTo(new HashSet<URL>(
                Arrays.asList(PNG_URL, SUBDOMAIN_URL))));
        assertThat(res.get(SUBDOMAIN_URL), equalTo(new HashSet<URL>(
                Arrays.asList(EXTERNAL_URL))));
        assertNull(res.get(PNG_URL));
    }
}