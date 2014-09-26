package simplecrawler;

import java.io.PrintStream;
import java.net.URL;
import java.util.Map;
import java.util.Set;

public class SitemapPrinter {
    private static final String NEWLINE = "\n";

    public static void print(Map<URL, Set<URL>> sitemap, PrintStream out) {
        sitemap.forEach((parent, children) -> {
            out.println(NEWLINE + parent);
            children.forEach(child -> out.println("\t- " + child));
        });
    }
}
