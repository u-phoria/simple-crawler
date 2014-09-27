package simplecrawler.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import simplecrawler.HtmlLinkExtractor;

import java.net.URI;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Improvements:
// - link extraction - right now we handle 3 types of links which covers most but almost certainly
//   not all use cases
// - better handling of invalid html docs -  jsoup is a liberal parser but will have
//   cases that break it
// - throughput - parsing wll be cpu bound, multiple  parallel workers (e.g. slightly fewer than
//   number of cores) pulling work off of a queue would improve things
// - streaming - currently we require full body html, a streaming parser would reduce
//   memory usage + possibly overall latency throughput by emitting early at expense of
//   complexity and possibly accuracy (no dom context etc)
// - support whitelist / blacklist domains
// - query strings - think about a bit more - right now we just throw them away
// - ...
public class SimpleHtmlLinkExtractor implements HtmlLinkExtractor {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleHtmlLinkExtractor.class);

    @Override
    public Set<URL> extractLinks(String html, final URL parent) {
        final Set<URL> result = new HashSet<URL>();
        Document doc = Jsoup.parse(html);

        doc.select("a[href]").forEach(a -> checkAndAdd(a.attr("href"), parent, result));
        doc.select("[src]").forEach(src -> checkAndAdd(src.attr("src"), parent, result));
        doc.select("link[href]").forEach((link -> checkAndAdd(link.attr("abs:href"), parent, result)));

        return result;
    }

    private void checkAndAdd(String urlSnippet, URL parent, Set<URL> result) {
        try {
            urlSnippet = removeAnchorAndQueryString(urlSnippet);
            URL url;
            if (new URI(urlSnippet).isAbsolute()) {
                url = new URL(urlSnippet);
            } else {
                url = new URL(parent, urlSnippet);
            }
            LOG.debug("Found URL {}", url);
            result.add(url);;
        } catch (Exception e) {
            LOG.error(String.format("Failed to extract URL snippet %s", urlSnippet), e);
        }
    }

    private String removeAnchorAndQueryString(String urlSnippet) {
        String[] noAnchor = urlSnippet.split("#");
        if (noAnchor.length == 0)
            return "";
        String[] noQuery = noAnchor[0].split("\\?");
        if (noQuery.length == 0)
            return "";
        return noQuery[0];
    }
}
