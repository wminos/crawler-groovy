package wminos

import groovy.util.logging.Slf4j

import javax.imageio.ImageIO
import java.nio.file.FileSystemException
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Created by wminos on 2015-08-16.
 */
@Slf4j
class GImageCrawlerApp {

    static void main(String... args) {

        System.out.printf("[Image Crawler]\n")

        final keyword
        if (args.length >= 1) {
            keyword = args[0]
            System.out.printf("- Keyword: %s\n", keyword)
        } else {
            System.out.printf("usage: ./GImageCrawlerApp run {search-keyword}\n")
            return;
        }

        for (final i in 0..1000) {
            int start = (i * 20)

            try {
                gatherImagesByKeyword(keyword, start)
            } catch (StopCrawlException e) {
                log.warn("StopCrawlException occurred: {}", e)
                break
            }
            catch (Throwable e) {
                log.warn "", e
            }
        }
    }

    private static void gatherImagesByKeyword(String keyword, int start) {
        final crawler = new GImageCrawler(keyword)
        final thumbnail_infos = crawler.gatherGoogleThumbnailInfos(start)

        thumbnail_infos.each { thumbnail_info ->

            // uncomment if you want download thumbnail
//            def thumbnail_local_path = crawler.downloadImageFile(DownloadType.GOOGLE_THUMBNAIL.getFolderName(), thumbnail_info.thumbnailImageUrl)
//            if (thumbnail_local_path == null) {
//                return;
//            }
//            log.info "thumbnail: {} -> {}", thumbnail_info.thumbnailImageUrl, thumbnail_local_path

            if ((thumbnail_info.originUrl ?: "").isEmpty()) {
                return
            }

            log.info ">> origin: '{}'", thumbnail_info.originUrl
            final origin_image_urls = WebImageCrawler.gatherImageUrls(thumbnail_info.originUrl)
            origin_image_urls.each { origin_image_url ->
                def origin_image_local_path = crawler.downloadImageFile(DownloadType.ORIGIN_IMAGE.getFolderName(), origin_image_url)

                if (origin_image_local_path == null) {
                    return
                }

                // validate image
                try {
                    final fis = new FileInputStream(origin_image_local_path)
                    final bufferedImage = ImageIO.read(fis)
                    fis.close()

                    if (bufferedImage != null) {
                        def width = bufferedImage.width
                        def height = bufferedImage.height

                        if (width * height <= 256 * 256) {
                            Files.delete(Paths.get(origin_image_local_path))
//                                log.debug "- delete {}, because image is small ({}x{})", origin_image_local_path, width, height
                        }
                    } else {
                        Files.delete(Paths.get(origin_image_local_path))
//                            log.debug "- delete {}, because invalid image", origin_image_local_path
                    }

                    log.info "image: {} -> {}", origin_image_url, origin_image_local_path
                } catch (FileSystemException e) {
                    log.warn "- cannot delete {}, because {}", origin_image_local_path, e.toString()
                } catch (Throwable e) {
                    log.warn "- exception occurred: {}", e.toString()
                }
            }
        }
    }
}
