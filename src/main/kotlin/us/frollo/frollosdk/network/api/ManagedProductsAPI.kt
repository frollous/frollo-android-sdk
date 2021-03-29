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

package us.frollo.frollosdk.network.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.QueryMap
import us.frollo.frollosdk.model.api.managedproduct.ManagedProductCreateRequest
import us.frollo.frollosdk.model.api.shared.PaginatedResponse
import us.frollo.frollosdk.model.coredata.managedproduct.ManagedProduct

internal interface ManagedProductsAPI {
    companion object {
        const val URL_AVAILABLE_PRODUCTS = "manage/products/available"
        const val URL_MANAGED_PRODUCTS = "manage/products"
        const val URL_MANAGED_PRODUCT = "manage/products/{product_id}"
    }

    @GET(URL_AVAILABLE_PRODUCTS)
    fun fetchAvailableProducts(@QueryMap options: Map<String, String>): Call<PaginatedResponse<ManagedProduct>>

    @GET(URL_MANAGED_PRODUCTS)
    fun fetchManagedProducts(@QueryMap options: Map<String, String>): Call<PaginatedResponse<ManagedProduct>>

    @GET(URL_MANAGED_PRODUCT)
    fun fetchManagedProduct(@Path("product_id") productId: Long): Call<ManagedProduct>

    @POST(URL_MANAGED_PRODUCTS)
    fun createManagedProduct(@Body request: ManagedProductCreateRequest): Call<ManagedProduct>

    @DELETE(URL_MANAGED_PRODUCT)
    fun deleteManagedProduct(@Path("product_id") productId: Long): Call<Void>
}
