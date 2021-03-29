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

package us.frollo.frollosdk.managedproducts

import us.frollo.frollosdk.base.PaginatedResultWithData
import us.frollo.frollosdk.base.PaginationInfo
import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.base.Result
import us.frollo.frollosdk.core.OnFrolloSDKCompletionListener
import us.frollo.frollosdk.extensions.enqueue
import us.frollo.frollosdk.extensions.fetchAvailableProducts
import us.frollo.frollosdk.extensions.fetchManagedProducts
import us.frollo.frollosdk.logging.Log
import us.frollo.frollosdk.model.api.managedproduct.ManagedProductCreateRequest
import us.frollo.frollosdk.model.api.shared.PaginatedResponse
import us.frollo.frollosdk.model.coredata.managedproduct.ManagedProduct
import us.frollo.frollosdk.network.NetworkService
import us.frollo.frollosdk.network.api.ManagedProductsAPI

/**
 * Manages Products
 */
class ManagedProducts(network: NetworkService) {

    companion object {
        private const val TAG = "ManagedProducts"
    }

    private val managedProductsAPI: ManagedProductsAPI = network.create(ManagedProductsAPI::class.java)

    /**
     * Lists all the products that are available for creation
     *
     * @param before ID of product to fetch list of products before (optional); used for pagination
     * @param after ID of product to fetch list of products after (optional); used for pagination
     * @param size Batch size of products to returned by API (optional)
     * @param completion: Completion handler with optional error if the request fails and list of [ManagedProduct] with pagination information if succeeds
     */
    fun fetchAvailableProducts(
        before: Long? = null,
        after: Long? = null,
        size: Long? = null,
        completion: OnFrolloSDKCompletionListener<PaginatedResultWithData<PaginationInfo, List<ManagedProduct>>>
    ) {
        managedProductsAPI.fetchAvailableProducts(after = after, before = before, size = size).enqueue { resource ->
            when (resource.status) {
                Resource.Status.SUCCESS -> {
                    handleManagedProducts(resource.data, completion)
                }
                Resource.Status.ERROR -> {
                    Log.e("$TAG#fetchAvailableProducts", resource.error?.localizedDescription)
                    completion.invoke(PaginatedResultWithData.Error(resource.error))
                }
            }
        }
    }

    /**
     * Lists all the products that has been created on the user account
     *
     * @param before ID of product to fetch list of products before (optional); used for pagination
     * @param after ID of product to fetch list of products after (optional); used for pagination
     * @param size Batch size of products to returned by API (optional)
     * @param completion: Completion handler with optional error if the request fails and list of [ManagedProduct] with pagination information if succeeds
     */
    fun fetchManagedProducts(
        before: Long? = null,
        after: Long? = null,
        size: Long? = null,
        completion: OnFrolloSDKCompletionListener<PaginatedResultWithData<PaginationInfo, List<ManagedProduct>>>
    ) {
        managedProductsAPI.fetchManagedProducts(after = after, before = before, size = size).enqueue { resource ->
            when (resource.status) {
                Resource.Status.SUCCESS -> {
                    handleManagedProducts(resource.data, completion)
                }
                Resource.Status.ERROR -> {
                    Log.e("$TAG#fetchManagedProducts", resource.error?.localizedDescription)
                    completion.invoke(PaginatedResultWithData.Error(resource.error))
                }
            }
        }
    }

    private fun handleManagedProducts(
        response: PaginatedResponse<ManagedProduct>? = null,
        completion: OnFrolloSDKCompletionListener<PaginatedResultWithData<PaginationInfo, List<ManagedProduct>>>
    ) {
        completion.invoke(
            PaginatedResultWithData.Success(
                paginationInfo = PaginationInfo(
                    after = response?.paging?.cursors?.after?.toLong(),
                    before = response?.paging?.cursors?.before?.toLong(),
                    total = response?.paging?.total
                ),
                data = response?.data
            )
        )
    }

    /**
     * Fetch a managed product using a product ID
     *
     * @param productId ID of [ManagedProduct] to fetch
     * @param completion Completion handler with optional error if the request fails else data if success
     */
    fun fetchManagedProduct(
        productId: Long,
        completion: OnFrolloSDKCompletionListener<Resource<ManagedProduct>>
    ) {
        managedProductsAPI.fetchManagedProduct(productId).enqueue { resource ->
            when (resource.status) {
                Resource.Status.SUCCESS -> {
                    completion.invoke(resource)
                }
                Resource.Status.ERROR -> {
                    Log.e("$TAG#fetchManagedProduct", resource.error?.localizedDescription)
                    completion.invoke(resource)
                }
            }
        }
    }

    /**
     * Create a managed product using a product from the available products
     *
     * @param productId ID of [ManagedProduct] to create
     * @param acceptedTermsConditionsIds: Array of IDs of [TermsConditions] for [ManagedProduct] to create
     * @param completion Completion handler with optional error if the request fails else data if success
     */
    fun createManagedProduct(
        productId: Long,
        acceptedTermsConditionsIds: List<Long>,
        completion: OnFrolloSDKCompletionListener<Resource<ManagedProduct>>
    ) {
        val request = ManagedProductCreateRequest(productId, acceptedTermsConditionsIds)
        managedProductsAPI.createManagedProduct(request).enqueue { resource ->
            when (resource.status) {
                Resource.Status.SUCCESS -> {
                    completion.invoke(resource)
                }
                Resource.Status.ERROR -> {
                    Log.e("$TAG#createManagedProduct", resource.error?.localizedDescription)
                    completion.invoke(resource)
                }
            }
        }
    }

    /**
     * Delete a specific [ManagedProduct]  by ID from the host
     *
     * @param productId ID of [ManagedProduct] to be deleted
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun deleteManagedProduct(productId: Long, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        managedProductsAPI.deleteManagedProduct(productId).enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#deleteManagedProduct", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    completion?.invoke(Result.success())
                }
            }
        }
    }
}
