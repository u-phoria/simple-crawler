package simplecrawler.parser;

import org.junit.Test;

import java.net.URL;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class SimpleHtmlLinkExtractorTest {
    private SimpleHtmlLinkExtractor simpleHtmlLinkExtractor = new SimpleHtmlLinkExtractor();

    @Test
    public void parseAnchorLink() throws Exception {
        String doc = "<html><a href='http://moo.com/hello'/></html>";
        Set<URL> res = simpleHtmlLinkExtractor.extractLinks(doc, null);
        assertThat(res.iterator().next().toString(), equalTo("http://moo.com/hello"));
    }

    @Test
    public void parseSrcWithParent() throws Exception {
        String doc = "<html><script type='text/javascript' src='greetings/hello.js'/></html>";
        Set<URL> res = simpleHtmlLinkExtractor.extractLinks(doc, new URL("http://moo.com/scripts/"));
        assertThat(res.iterator().next().toString(), equalTo("http://moo.com/scripts/greetings/hello.js"));
    }

    @Test
    public void parseLink() throws Exception {
        String doc = "<html><link rel='stylesheet' href='http://moo.com/styles/hello.css'/></html>";
        Set<URL> res = simpleHtmlLinkExtractor.extractLinks(doc, new URL("http://moo.com/"));
        assertThat(res.iterator().next().toString(), equalTo("http://moo.com/styles/hello.css"));
    }

    @Test
    public void excludeAnchor() throws Exception {
        String doc = "<html><a href='hello#anch'/></html>";
        Set<URL> res = simpleHtmlLinkExtractor.extractLinks(doc, new URL("http://moo.com"));
        assertThat(res.iterator().next().toString(), equalTo("http://moo.com/hello"));
    }

    @Test
    public void linkInBrokenHtml() throws Exception {
        String doc = "<html><a href=hello#anch'</tml>";
        Set<URL> res = simpleHtmlLinkExtractor.extractLinks(doc, new URL("http://moo.com"));
        assertThat(res.iterator().next().toString(), equalTo("http://moo.com/hello"));
    }

    @Test
    public void excludeLoneAnchor() throws Exception {
        String doc = "<html><a href='#'/></html>";
        Set<URL> res = simpleHtmlLinkExtractor.extractLinks(doc, new URL("http://moo.com"));
        assertThat(res.iterator().next().toString(), equalTo("http://moo.com"));
    }

    @Test
    public void excludeQueryString() throws Exception {
        String doc = "<html><a href='hello?a=1'/></html>";
        Set<URL> res = simpleHtmlLinkExtractor.extractLinks(doc, new URL("http://moo.com"));
        assertThat(res.iterator().next().toString(), equalTo("http://moo.com/hello"));
    }

    @Test
    public void excludeEmptyQueryString() throws Exception {
        String doc = "<html><a href='?'/></html>";
        Set<URL> res = simpleHtmlLinkExtractor.extractLinks(doc, new URL("http://moo.com"));
        assertThat(res.iterator().next().toString(), equalTo("http://moo.com"));
    }
}