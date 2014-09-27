package simplecrawler.cli;

import simplecrawler.HtmlLinkExtractor;
import simplecrawler.SingleDomainCrawler;
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
        if (argv.length != 1) {
            System.err.println("Usage: java -jar <jar name> <root url>");
            System.exit(1);
        }

        App app = new App();
        app.run(argv[0]);
    }
}
