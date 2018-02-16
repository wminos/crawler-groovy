package wminos

import groovy.util.logging.Slf4j
import org.apache.http.client.HttpResponseException
import org.apache.http.client.fluent.Request
import org.apache.http.client.utils.URIBuilder
import org.apache.http.entity.ContentType
import org.apache.http.util.EntityUtils
import org.jsoup.Jsoup

import java.nio.file.Files
import java.nio.file.Paths
import java.util.zip.CRC32

/**
 * Created by wminos on 2015-08-16.
 */
@Slf4j
class GImageCrawler {

    private String keyword

    GImageCrawler(String keyword) {
        this.keyword = keyword
    }

    List<GThumbnailInfo> gatherGoogleThumbnailInfos(int start, int num = 20) {
        final uri = new URIBuilder()
                .setScheme("http")
                .setHost("www.google.com")
                .setPath("/search")
                .setParameter("tbm", "isch")
                .setParameter("safe", "off")
                .setParameter("sout", "1") // non-autoscroll
                .setParameter("start", start.toString())
                .setParameter("num", num.toString())
                .setParameter("q", keyword)
                .build()
        log.debug "url: {}", uri

        return gatherGoogleThumbnailInfos__(uri)
    }

    private static List<GThumbnailInfo> gatherGoogleThumbnailInfos__(URI uri) {

        final String content
        try {
            content = Request.Get(uri).execute().returnContent().asString()
        } catch (HttpResponseException e) {
//            if (e.getStatusCode() == HttpStatus.SC_SERVICE_UNAVAILABLE) {
//                throw new StopCrawlException(e);
//            }

            throw new StopCrawlException(e)
        }
        log.debug "content length: {}", content.length()

        final doc = Jsoup.parse(content, uri.toString())
        final thumbnail_elements = doc.select("img[src]")

        def thumbnail_infos = thumbnail_elements.collect { img_element ->

            final thumbnail_img_src = img_element.absUrl("src")
            def origin_href = null

            final parent_element = img_element.parent()
            if (parent_element != null) {
                final origin_link_href = parent_element.attr("href") // /url?q={origin_href}
                if (origin_link_href != null) {
                    final params = new URIBuilder(origin_link_href).getQueryParams()
                    final param_q_pair = params.find { pair -> pair.name == 'q' }
                    origin_href = param_q_pair != null ? param_q_pair.value : null
                }
            }

            new GThumbnailInfo(thumbnail_img_src, origin_href)
        }
        return thumbnail_infos
    }

    // TODO async
    String downloadImageFile(String folderPath, String uri) {

        final response

        try {
            response = Request.Get(uri).execute()
        } catch (IllegalArgumentException ignored) {
            return null
        }

        final httpResponse = response.returnResponse()
        final httpEntity = httpResponse.entity

        if (httpEntity == null) {
            return null
        }

        final contentType = ContentType.getOrDefault(httpEntity)
        final bytes = EntityUtils.toByteArray(httpEntity)
        final allHeaders = httpResponse.allHeaders

        String fileExt = getDefaultFileExtension(contentType)

        if (fileExt.isEmpty()) {
            log.warn("missing content type: {} @ {}", contentType.mimeType, uri)
            fileExt = ".webp"
        }

        log.trace "file ext: {}", fileExt
        CRC32 checksum = new CRC32()
        checksum.update(bytes, 0, bytes.length)
        long checksumValue = checksum.value

//        String fileNameWithoutExt = keyword + "." + checksumValue
        final fileNameWithoutExt = checksumValue
        final fileNameWithExt = fileNameWithoutExt + fileExt
        final downloadPath = getDownloadPath(Paths.get(keyword, folderPath, fileNameWithExt) as String)

        log.trace "download path: {}", downloadPath

//        def stream = new FileOutputStream(downloadPath)
//        stream.write(bytes)
//        stream.close()

        new FileOutputStream(downloadPath).withCloseable {
            it.write(bytes)
        }

        downloadPath
    }

    private static String getDownloadPath(String filePath) {

        final fullPath = Paths.get(System.getProperty("user.home"), "google-crawled", filePath)
        log.trace "path on: {}", fullPath

        Files.createDirectories fullPath.parent
        return fullPath
    }

    private static String getDefaultFileExtension(ContentType contentType) {

        switch (contentType.mimeType) {
            case "image/jpeg": return ".jpg"
            case "image/jpg": return ".jpg"
            case "image/png": return ".png"
            case "image/gif": return ".gif"
        }

        return ""
    }
}
