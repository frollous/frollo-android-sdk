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
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.QueryMap
import us.frollo.frollosdk.model.api.address.AddressAutocomplete
import us.frollo.frollosdk.model.api.address.AddressRequest
import us.frollo.frollosdk.model.api.address.AddressResponse
import us.frollo.frollosdk.model.coredata.address.AddressAutocompleteDetails

internal interface AddressAPI {
    companion object {
        const val URL_ADDRESSES = "addresses"
        const val URL_ADDRESS = "addresses/{address_id}"
        const val URL_ADDRESS_AUTOCOMPLETE = "addresses/autocomplete"
        const val URL_ADDRESS_AUTOCOMPLETE_DETAILS = "addresses/autocomplete/{address_id}"
    }

    @GET(URL_ADDRESSES)
    fun fetchAddresses(): Call<List<AddressResponse>>

    @GET(URL_ADDRESS)
    fun fetchAddress(@Path("address_id") cardId: Long): Call<AddressResponse>

    @POST(URL_ADDRESSES)
    fun createAddress(@Body request: AddressRequest): Call<AddressResponse>

    @PUT(URL_ADDRESS)
    fun updateAddress(@Path("address_id") goalId: Long, @Body request: AddressRequest): Call<AddressResponse>

    @DELETE(URL_ADDRESS)
    fun deleteAddress(@Path("address_id") goalId: Long): Call<Void>

    @POST(URL_ADDRESS_AUTOCOMPLETE)
    fun fetchSuggestedAddresses(@QueryMap queryParams: Map<String, String>): Call<List<AddressAutocomplete>>

    @GET(URL_ADDRESS_AUTOCOMPLETE_DETAILS)
    fun fetchSuggestedAddress(@Path("address_id") addressId: String): Call<AddressAutocompleteDetails>
}
