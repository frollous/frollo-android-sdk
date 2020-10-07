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

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.sqlite.db.SimpleSQLiteQuery
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.base.Result
import us.frollo.frollosdk.base.SimpleSQLiteQueryBuilder
import us.frollo.frollosdk.core.OnFrolloSDKCompletionListener
import us.frollo.frollosdk.database.SDKDatabase
import us.frollo.frollosdk.extensions.enqueue
import us.frollo.frollosdk.extensions.fetchImages
import us.frollo.frollosdk.extensions.sqlForImageIds
import us.frollo.frollosdk.extensions.sqlForImages
import us.frollo.frollosdk.logging.Log
import us.frollo.frollosdk.mapping.toImage
import us.frollo.frollosdk.model.api.images.ImageResponse
import us.frollo.frollosdk.model.coredata.images.Image
import us.frollo.frollosdk.network.NetworkService
import us.frollo.frollosdk.network.api.ImagesAPI

/**
 * Manages caching and refreshing of images
 */
class Images(network: NetworkService, internal val db: SDKDatabase) {

    companion object {
        private const val TAG = "Images"
    }

    private val imagesAPI: ImagesAPI = network.create(ImagesAPI::class.java)

    // Image

    /**
     * Fetch image by ID from the cache
     *
     * @param imageId Unique image ID to fetch
     *
     * @return LiveData object of Resource<Image> which can be observed using an Observer for future changes as well.
     */
    fun fetchImage(imageId: Long): LiveData<Resource<Image>> =
        Transformations.map(db.images().load(imageId)) { model ->
            Resource.success(model)
        }

    /**
     * Fetch images from the cache
     *
     * @param imageType Image type to find matching Images for (optional). Values can be - "goal", "challenge"
     *
     * @return LiveData object of Resource<List<Image> which can be observed using an Observer for future changes as well.
     */
    fun fetchImages(imageType: String? = null): LiveData<Resource<List<Image>>> =
        Transformations.map(
            db.images().loadByQuery(
                sqlForImages(imageType)
            )
        ) { models ->
            Resource.success(models)
        }

    /**
     * Advanced method to fetch images by SQL query from the cache
     *
     * @param query SimpleSQLiteQuery: Select query which fetches images from the cache
     *
     * Note: Please check [SimpleSQLiteQueryBuilder] to build custom SQL queries
     *
     * @return LiveData object of Resource<List<Image>> which can be observed using an Observer for future changes as well.
     */
    fun fetchImages(query: SimpleSQLiteQuery): LiveData<Resource<List<Image>>> =
        Transformations.map(db.images().loadByQuery(query)) { model ->
            Resource.success(model)
        }

    /**
     * Refresh all available images from the host.
     *
     * @param imageType Filter images by type (optional). Values can be - "goal", "challenge"
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun refreshImages(
        imageType: String? = null,
        completion: OnFrolloSDKCompletionListener<Result>? = null
    ) {
        imagesAPI.fetchImages(imageType).enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#refreshImages", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    handleImagesResponse(
                        response = resource.data,
                        imageType = imageType,
                        completion = completion
                    )
                }
            }
        }
    }

    // Response Handlers

    private fun handleImagesResponse(
        response: List<ImageResponse>?,
        imageType: String?,
        completion: OnFrolloSDKCompletionListener<Result>?
    ) {
        response?.let {
            doAsync {
                val models = mapImagesResponse(response)

                db.images().insertAll(*models.toTypedArray())

                val apiIds = models.map { it.imageId }.toHashSet()
                val allImageIds = db.images().getIdsByQuery(sqlForImageIds(imageType)).toHashSet()
                val staleIds = allImageIds.minus(apiIds)

                if (staleIds.isNotEmpty()) {
                    db.images().deleteMany(staleIds.toLongArray())
                }

                uiThread { completion?.invoke(Result.success()) }
            }
        } ?: run { completion?.invoke(Result.success()) } // Explicitly invoke completion callback if response is null.
    }

    private fun mapImagesResponse(models: List<ImageResponse>): List<Image> {
        return models.map { it.toImage() }
    }
}
