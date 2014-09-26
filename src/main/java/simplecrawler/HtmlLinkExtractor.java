package simplecrawler;

import java.net.URL;
import java.util.List;
import java.util.Set;

public interface HtmlLinkExtractor {
    Set<URL> extractLinks(String html, URL parent);
}
