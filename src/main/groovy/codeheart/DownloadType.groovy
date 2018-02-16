package wminos

/**
 * Created by wminos on 2015-08-16.
 */
enum DownloadType {

    GOOGLE_THUMBNAIL,
    ORIGIN_IMAGE

    String getFolderName() {
        switch (this) {
            case GOOGLE_THUMBNAIL: return "thumbnail"
            case ORIGIN_IMAGE: return "image"
            default: return "default"
        }
    }
}
