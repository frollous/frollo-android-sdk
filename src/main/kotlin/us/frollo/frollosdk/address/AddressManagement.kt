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

package us.frollo.frollosdk.address

import androidx.lifecycle.LiveData
import androidx.sqlite.db.SimpleSQLiteQuery
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.base.Result
import us.frollo.frollosdk.base.SimpleSQLiteQueryBuilder
import us.frollo.frollosdk.core.OnFrolloSDKCompletionListener
import us.frollo.frollosdk.database.SDKDatabase
import us.frollo.frollosdk.extensions.enqueue
import us.frollo.frollosdk.extensions.fetchSuggestedAddresses
import us.frollo.frollosdk.logging.Log
import us.frollo.frollosdk.mapping.toAddress
import us.frollo.frollosdk.model.api.address.AddressAutocomplete
import us.frollo.frollosdk.model.api.address.AddressRequest
import us.frollo.frollosdk.model.api.address.AddressResponse
import us.frollo.frollosdk.model.coredata.address.Address
import us.frollo.frollosdk.model.coredata.address.AddressAutocompleteDetails
import us.frollo.frollosdk.network.NetworkService
import us.frollo.frollosdk.network.api.AddressAPI

/** Manages all aspects of Addresses */
class AddressManagement(network: NetworkService, internal val db: SDKDatabase) {

    companion object {
        private const val TAG = "Addresses"
    }

    private val addressAPI: AddressAPI = network.create(AddressAPI::class.java)

    /**
     * Fetch address by ID from the cache
     *
     * @param addressId Unique address ID to fetch
     *
     * @return LiveData object of Address which can be observed using an Observer for future changes as well.
     */
    fun fetchAddress(addressId: Long): LiveData<Address?> {
        return db.addresses().load(addressId)
    }

    /**
     * Fetch addresses from the cache
     *
     * @return LiveData object of List<Address> which can be observed using an Observer for future changes as well.
     */
    fun fetchAddresses(): LiveData<List<Address>> {
        return db.addresses().load()
    }

    /**
     * Advanced method to fetch addresses by SQL query from the cache
     *
     * @param query SimpleSQLiteQuery: Select query which fetches addresses from the cache
     *
     * Note: Please check [SimpleSQLiteQueryBuilder] to build custom SQL queries
     *
     * @return LiveData object of List<Address> which can be observed using an Observer for future changes as well.
     */
    fun fetchAddresses(query: SimpleSQLiteQuery): LiveData<List<Address>> {
        return db.addresses().loadByQuery(query)
    }

    /**
     * Refresh a specific address by ID from the host
     *
     * @param addressId ID of the address to fetch
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun refreshAddress(addressId: Long, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        addressAPI.fetchAddress(addressId).enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#refreshAddress", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    handleAddressResponse(response = resource.data, completion = completion)
                }
            }
        }
    }

    /**
     * Refresh all available address from the host.
     *
     * @param completion Optional completion handler with optional error if the request fails and list of address if succeeds
     */
    fun refreshAddresses(completion: OnFrolloSDKCompletionListener<Resource<List<Address>>>? = null) {
        addressAPI.fetchAddresses().enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#refreshAddresses", resource.error?.localizedDescription)
                    completion?.invoke(Resource.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    handleAddressesResponse(resource.data, completion)
                }
            }
        }
    }

    /**
     * Create a new address on the host
     *
     * @param unitNumber Unit number (Optional)
     * @param buildingName Building name (Optional)
     * @param streetNumber Street number (Optional)
     * @param streetName Street name (Optional)
     * @param streetType Street type (Optional)
     * @param suburb Suburb (Optional)
     * @param town Town (Optional)
     * @param region Region (Optional)
     * @param state State (Optional)
     * @param country Country in short form Eg: AUS (Optional)
     * @param postcode Postcode (Optional)
     * @param completion Optional completion handler with optional error if the request fails else ID of the Address created if success
     */
    fun createAddress(
        unitNumber: String? = null,
        buildingName: String? = null,
        streetNumber: String? = null,
        streetName: String? = null,
        streetType: String? = null,
        suburb: String? = null,
        town: String? = null,
        region: String? = null,
        state: String? = null,
        country: String? = null,
        postcode: String? = null,
        completion: OnFrolloSDKCompletionListener<Resource<Long>>? = null
    ) {
        val request = AddressRequest(
            unitNumber = unitNumber,
            buildingName = buildingName,
            streetNumber = streetNumber,
            streetName = streetName,
            streetType = streetType,
            suburb = suburb,
            town = town,
            region = region,
            state = state,
            country = country,
            postcode = postcode
        )

        addressAPI.createAddress(request).enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#createAddress", resource.error?.localizedDescription)
                    completion?.invoke(Resource.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    handleAddressResponse(response = resource.data, completionWithData = completion)
                }
            }
        }
    }

    /**
     * Update a address on the host
     *
     * @param addressId ID of the address to be updated
     * @param unitNumber Unit number (Optional)
     * @param buildingName Building name (Optional)
     * @param streetNumber Street number (Optional)
     * @param streetName Street name (Optional)
     * @param streetType Street type (Optional)
     * @param suburb Suburb (Optional)
     * @param town Town (Optional)
     * @param region Region (Optional)
     * @param state State (Optional)
     * @param country Country in short form Eg: AUS (Optional)
     * @param postcode Postcode (Optional)
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun updateAddress(
        addressId: Long,
        unitNumber: String? = null,
        buildingName: String? = null,
        streetNumber: String? = null,
        streetName: String? = null,
        streetType: String? = null,
        suburb: String? = null,
        town: String? = null,
        region: String? = null,
        state: String? = null,
        country: String? = null,
        postcode: String? = null,
        completion: OnFrolloSDKCompletionListener<Result>? = null
    ) {
        val request = AddressRequest(
            unitNumber = unitNumber,
            buildingName = buildingName,
            streetNumber = streetNumber,
            streetName = streetName,
            streetType = streetType,
            suburb = suburb,
            town = town,
            region = region,
            state = state,
            country = country,
            postcode = postcode
        )

        addressAPI.updateAddress(addressId, request).enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#updateAddress", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    handleAddressResponse(response = resource.data, completion = completion)
                }
            }
        }
    }

    /**
     * Delete an address on the host
     *
     * @param addressId ID of the address to be deleted
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun deleteAddress(
        addressId: Long,
        completion: OnFrolloSDKCompletionListener<Result>? = null
    ) {
        addressAPI.deleteAddress(addressId).enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#deleteAddress", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    db.addresses().delete(addressId)
                    completion?.invoke(Result.success())
                }
            }
        }
    }

    /**
     * Get addresses list that matches the query string from the host
     *
     * @param query String to match address
     * @param max Maximum number of items to fetch. Should be between 10 and 100; defaults to 20.
     * @param completion Completion handler with optional error if the request fails or list of addresses is success
     */
    fun fetchSuggestedAddresses(
        query: String,
        max: Int = 20,
        completion: OnFrolloSDKCompletionListener<Resource<List<AddressAutocomplete>>>
    ) {
        addressAPI.fetchSuggestedAddresses(query, max).enqueue { resource ->
            if (resource.status == Resource.Status.ERROR) {
                Log.e("$TAG#addressAutocomplete", resource.error?.localizedDescription)
            }
            completion.invoke(resource)
        }
    }

    /**
     * Get suggested address by ID from the host
     *
     * @param addressId ID of the address to get the details
     * @param completion Completion handler with optional error if the request fails or address details is success
     */
    fun fetchSuggestedAddress(
        addressId: String,
        completion: OnFrolloSDKCompletionListener<Resource<AddressAutocompleteDetails>>
    ) {
        addressAPI.fetchSuggestedAddress(addressId).enqueue { resource ->
            if (resource.status == Resource.Status.ERROR) {
                Log.e("$TAG#fetchAddress", resource.error?.localizedDescription)
            }
            completion.invoke(resource)
        }
    }

    // Response Handlers

    private fun handleAddressResponse(
        response: AddressResponse?,
        completion: OnFrolloSDKCompletionListener<Result>? = null,
        completionWithData: OnFrolloSDKCompletionListener<Resource<Long>>? = null
    ) {
        response?.let {
            doAsync {
                val model = response.toAddress()

                db.addresses().insert(model)

                uiThread {
                    completion?.invoke(Result.success())
                    completionWithData?.invoke(Resource.success(response.addressId))
                }
            }
        } ?: run {
            completion?.invoke(Result.success())
            completionWithData?.invoke(Resource.success(null))
        } // Explicitly invoke completion callback if response is null.
    }

    private fun handleAddressesResponse(
        response: List<AddressResponse>?,
        completion: OnFrolloSDKCompletionListener<Resource<List<Address>>>?
    ) {
        var models = listOf<Address>()
        response?.let {
            doAsync {
                models = response.map { it.toAddress() }

                db.addresses().insertAll(*models.toTypedArray())

                val apiIds = models.map { it.addressId }.toHashSet()
                val allAddressIds = db.addresses().getIds().toHashSet()
                val staleIds = allAddressIds.minus(apiIds)

                if (staleIds.isNotEmpty()) {
                    db.addresses().deleteMany(staleIds.toLongArray())
                }

                uiThread { completion?.invoke(Resource.success(models)) }
            }
        } ?: run { completion?.invoke(Resource.success(models)) } // Explicitly invoke completion callback if response is null.
    }
}
