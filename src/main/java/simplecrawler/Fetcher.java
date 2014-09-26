package simplecrawler;

import java.net.URL;
import java.util.List;
import java.util.function.Consumer;


public interface Fetcher {

    void fetch(URL url, Consumer<FetchResult> resultConsumer, Consumer<Exception> exceptionConsumer);

    public class FetchResult {
        private URL url;
        private String contentType;
        private String body;

        public FetchResult(URL url, String contentType, String body) {
            this.url = url;
            this.contentType = contentType;
            this.body = body;
        }

        public URL getUrl() {
            return url;
        }

        public String getContentType() {
            return contentType;
        }

        public String getBody() {
            return body;
        }
    }
}
