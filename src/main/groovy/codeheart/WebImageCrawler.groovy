package wminos

import org.apache.http.client.fluent.Request
import org.jsoup.Jsoup

/**
 * Created by wminos on 2015-08-16.
 */
class WebImageCrawler {

    static List<String> gatherImageUrls(String htmlUrl) {

        if (htmlUrl == null) {
            return []
        }

        final content = Request.Get(htmlUrl).execute().returnContent().asString()
        final doc = Jsoup.parse(content, htmlUrl.toString())
        final image_elements = doc.select("img[src]")
        final image_urls = image_elements.collect { image_element -> image_element.absUrl("src") }.findAll {
            !(it ?: "").isEmpty()
        }
        return image_urls
    }
}
