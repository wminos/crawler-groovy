import wminos.DownloadType
import wminos.GImageCrawler
import groovy.util.logging.Slf4j
import org.apache.http.client.fluent.Request
import org.apache.http.client.utils.URIBuilder
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.junit.Test

@Slf4j
class WebTests {

    @Test
    void testJsoupBasic() {
        def doc = Jsoup.connect("http://example.com").get() as Document
        def links = doc.select("a[href]") as Elements
        log.info("doc: {}", doc)
        log.info("links: {}", links)
    }

    @Test
    void testJsoupWithApache() {
        /**
         'tbm' => 'isch', # type : image search
         'safe' => 'off',
         'sout' => '1', # non-auto scroll
         'start' => page * 20, # start image index ( 20 )
         'q' => @keyword

         */

        def keyword = "cute"
        def start = 0

        final uri = new URIBuilder()
                .setScheme("http")
                .setHost("www.google.com")
                .setPath("/search")
                .setParameter("tbm", "isch")
                .setParameter("safe", "off")
                .setParameter("sout", "1") // non-autoscroll
                .setParameter("start", start.toString())
                .setParameter("q", keyword)
                .build();
        log.info "url: {}", uri;

        final content = Request.Get(uri).execute().returnContent().asString()
        log.info "content length: {}", content.length();

        final doc = Jsoup.parse(content, uri.toString())
        final img_elements = doc.select("img[src]") as Elements

        img_elements.each { imgElement ->
            log.info "img src: {}", imgElement.absUrl("src")
        }
    }

    @Test
    void testDownload() {
        def url = "http://t3.gstatic.com/images?q=tbn:ANd9GcTmlQ6mrWnjpJ3VEFFlQNqmf8NKAsfaYYzTbTIpMXZAARK09ES-qzuYhpdO";
        new GImageCrawler("test").downloadImageFile(DownloadType.GOOGLE_THUMBNAIL.getFolderName(), url);
    }
}
