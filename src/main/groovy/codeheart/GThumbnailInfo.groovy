package wminos

import groovy.transform.TupleConstructor

/**
 * Created by wminos on 2015-08-16.
 */
@TupleConstructor(force = true)
class GThumbnailInfo {

    String thumbnailImageUrl // absolute url

    String originUrl // source origin url (not image)


    @Override
    String toString() {
        return "GThumbnailInfo{" +
                "thumbnailImageUrl='" + thumbnailImageUrl + '\'' +
                ", originUrl='" + originUrl + '\'' +
                '}'
    }
}
