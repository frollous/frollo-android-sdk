/*
 * Copyright 2019 Frollo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package us.frollo.frollosdk.images

import androidx.sqlite.db.SimpleSQLiteQuery
import io.reactivex.Observable
import us.frollo.frollosdk.base.SimpleSQLiteQueryBuilder
import us.frollo.frollosdk.extensions.sqlForImages
import us.frollo.frollosdk.model.coredata.images.Image

// Image

/**
 * Fetch image by ID from the cache
 *
 * @param imageId Unique image ID to fetch
 *
 * @return Rx Observable object of Image which can be observed using an Observer for future changes as well.
 */
fun Images.fetchImageRx(imageId: Long): Observable<Image?> {
    return db.images().loadRx(imageId)
}

/**
 * Fetch images from the cache
 *
 * @param imageType Array of image types to find matching Images for (optional)
 *
 * @return Rx Observable object of List<Image> which can be observed using an Observer for future changes as well.
 */
fun Images.fetchImagesRx(imageType: String? = null): Observable<List<Image>> {
    return db.images().loadByQueryRx(
        sqlForImages(imageType)
    )
}

/**
 * Advanced method to fetch images by SQL query from the cache
 *
 * @param query SimpleSQLiteQuery: Select query which fetches images from the cache
 *
 * Note: Please check [SimpleSQLiteQueryBuilder] to build custom SQL queries
 *
 * @return Rx Observable object of List<Image> which can be observed using an Observer for future changes as well.
 */
fun Images.fetchImagesRx(query: SimpleSQLiteQuery): Observable<List<Image>> {
    return db.images().loadByQueryRx(query)
}
