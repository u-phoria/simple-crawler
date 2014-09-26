package simplecrawler;

import simplecrawler.fetcher.AsyncFetcher;
import simplecrawler.parser.SimpleHtmlLinkExtractor;

import java.net.URL;
import java.util.Map;
import java.util.Set;

public class App {
    public void run(String rootUrl) throws Exception {
        HtmlLinkExtractor linkExtractor = new SimpleHtmlLinkExtractor();

        Map<URL, Set<URL>> sitemap;
        try (AsyncFetcher fetcher = new AsyncFetcher()) {
            SingleDomainCrawler crawler = new SingleDomainCrawler(fetcher, linkExtractor);
            sitemap = crawler.run(new URL(rootUrl));
        }
        SitemapPrinter.print(sitemap, System.out);
    }

    public static void main(String[] argv) throws Exception {
        App app = new App();
        app.run(argv[0]);
    }
}
