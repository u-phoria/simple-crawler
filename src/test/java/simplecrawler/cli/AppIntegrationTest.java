package simplecrawler.cli;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import org.junit.Test;
import simplecrawler.cli.App;

import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;

// Improvements:
// - test error scenarios (eg metwork error, incorrect content type, empty response)
// - auto-discover free port for dummy web server
// - ...
public class AppIntegrationTest {
    public static final String TEXT_HTML = "text/html";
    private static final String TEXT_PLAIN = "text/plain";
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(28089);

    @Test
    public void test() throws Exception  {
        stubResponse("/", TEXT_HTML,
                "<link href='http://localhost:28089/styles/main.css'/>" +
                "<a href='goodpage.html'></a>" +
                "<a href='/badpage.html'></a>");
        stubResponse("/styles/main.css", TEXT_PLAIN, "a {}");
        stubResponse("/goodpage.html", TEXT_HTML,
                "<a href='http://somewhere.else.com/'/>" +
                "<a href='/'/>" +
                "<a href='/goodpage.html'/>");
        stubResponse("/badpage.html", TEXT_HTML, "<broken html");

        App app = new App();
        app.run("http://localhost:28089/");
    }

    private void stubResponse(String resource, String contentType, String content) {
        stubFor(get(urlEqualTo(resource))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", contentType)
                        .withBody(content)));
    }

}
