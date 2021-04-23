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

package us.frollo.frollosdk.contacts

import androidx.lifecycle.LiveData
import androidx.sqlite.db.SimpleSQLiteQuery
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import us.frollo.frollosdk.base.PaginatedResult
import us.frollo.frollosdk.base.PaginationInfo
import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.base.Result
import us.frollo.frollosdk.base.SimpleSQLiteQueryBuilder
import us.frollo.frollosdk.core.OnFrolloSDKCompletionListener
import us.frollo.frollosdk.database.SDKDatabase
import us.frollo.frollosdk.extensions.enqueue
import us.frollo.frollosdk.extensions.fetchContacts
import us.frollo.frollosdk.extensions.sqlForContactIdsToGetStaleIds
import us.frollo.frollosdk.extensions.sqlForContacts
import us.frollo.frollosdk.logging.Log
import us.frollo.frollosdk.mapping.toContact
import us.frollo.frollosdk.model.api.contacts.ContactCreateUpdateRequest
import us.frollo.frollosdk.model.api.contacts.ContactInternationalCreateUpdateRequest
import us.frollo.frollosdk.model.api.contacts.ContactResponse
import us.frollo.frollosdk.model.coredata.contacts.CRNType
import us.frollo.frollosdk.model.coredata.contacts.Contact
import us.frollo.frollosdk.model.coredata.contacts.PayIDType
import us.frollo.frollosdk.model.coredata.contacts.PaymentDetails
import us.frollo.frollosdk.model.coredata.contacts.PaymentMethod
import us.frollo.frollosdk.network.NetworkService
import us.frollo.frollosdk.network.api.ContactsAPI

/** Manages user contacts */
class Contacts(network: NetworkService, internal val db: SDKDatabase) {

    companion object {
        private const val TAG = "Contacts"
    }

    private val contactsAPI: ContactsAPI = network.create(ContactsAPI::class.java)

    // Contact

    /**
     * Fetch contact by ID from the cache
     *
     * @param contactId Unique contact ID to fetch
     *
     * @return LiveData object of Contact which can be observed using an Observer for future changes as well.
     */
    fun fetchContact(contactId: Long): LiveData<Contact?> {
        return db.contacts().load(contactId)
    }

    /**
     * Fetch contacts from the cache
     *
     * @param paymentMethod Filter by [PaymentMethod] of the contact (optional)
     *
     * @return LiveData object of List<Contact> which can be observed using an Observer for future changes as well.
     */
    fun fetchContacts(paymentMethod: PaymentMethod? = null): LiveData<List<Contact>> {
        return db.contacts().loadByQuery(sqlForContacts(paymentMethod))
    }

    /**
     * Advanced method to fetch contacts by SQL query from the cache
     *
     * @param query SimpleSQLiteQuery: Select query which fetches contacts from the cache
     *
     * Note: Please check [SimpleSQLiteQueryBuilder] to build custom SQL queries
     *
     * @return LiveData object of List<Contact> which can be observed using an Observer for future changes as well.
     */
    fun fetchContacts(query: SimpleSQLiteQuery): LiveData<List<Contact>> {
        return db.contacts().loadByQuery(query)
    }

    /**
     * Refresh a specific contact by ID from the host
     *
     * @param contactId ID of the contact to fetch
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun refreshContact(contactId: Long, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        contactsAPI.fetchContact(contactId).enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#refreshContact", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    handleContactResponse(response = resource.data, completion = completion)
                }
            }
        }
    }

    /**
     * Refresh contacts from the host
     *
     * @param paymentMethod Filter contacts by their [PaymentMethod]
     * @param before Contact ID to fetch before this contact (optional)
     * @param after Contact ID to fetch upto this contact (optional)
     * @param size Count of objects to returned from the API (page size) (optional)
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun refreshContactsWithPagination(
        paymentMethod: PaymentMethod? = null,
        after: Long? = null,
        before: Long? = null,
        size: Long? = null,
        completion: OnFrolloSDKCompletionListener<PaginatedResult<PaginationInfo>>? = null
    ) {
        contactsAPI.fetchContacts(
            paymentMethod = paymentMethod,
            after = after,
            before = before,
            size = size
        ).enqueue { resource ->
            when (resource.status) {
                Resource.Status.SUCCESS -> {
                    val response = resource.data
                    handleContactsWithPaginationResponse(
                        response = response?.data,
                        paymentMethod = paymentMethod,
                        before = response?.paging?.cursors?.before?.toLong(),
                        after = response?.paging?.cursors?.after?.toLong(),
                        completion = completion
                    )
                }
                Resource.Status.ERROR -> {
                    Log.e("$TAG#refreshContactsWithPagination", resource.error?.localizedDescription)
                    completion?.invoke(PaginatedResult.Error(resource.error))
                }
            }
        }
    }

    /**
     * Create a new PayAnyone contact on the host
     *
     * @param name Name of the contact; default value will be nickName (Optional)
     * @param nickName Nickname of the contact
     * @param description Description of the contact (Optional)
     * @param accountName Account name of the payAnyone contact
     * @param bsb BSB of the payAnyone contact
     * @param accountNumber Account number of the payAnyone contact
     * @param completion Optional completion handler with optional error if the request fails else ID of the Contact created if success
     */
    fun createPayAnyoneContact(
        name: String? = null,
        nickName: String,
        description: String? = null,
        accountName: String,
        bsb: String,
        accountNumber: String,
        completion: OnFrolloSDKCompletionListener<Resource<Long>>? = null
    ) {
        val request = ContactCreateUpdateRequest(
            name = name ?: nickName,
            nickName = nickName,
            description = description,
            paymentMethod = PaymentMethod.PAY_ANYONE,
            paymentDetails = PaymentDetails.PayAnyone(
                accountHolder = accountName,
                bsb = bsb,
                accountNumber = accountNumber
            )
        )
        createContact(request, completion)
    }

    /**
     * Create a new BPay contact on the host
     *
     * @param name Name of the contact; default value will be nickName (Optional)
     * @param nickName Nickname of the contact
     * @param description Description of the contact (Optional)
     * @param billerCode Biller Code of the BPAY contact
     * @param crn CRN of the BPAY contact
     * @param billerName Biller Name of the BPAY contact
     * @param crnType Type of the Biller's CRN; defaulted to Fixed CRN. refer to [CRNType]
     * @param completion Optional completion handler with optional error if the request fails else ID of the Contact created if success
     */
    fun createBPayContact(
        name: String? = null,
        nickName: String,
        description: String? = null,
        billerCode: String,
        crn: String,
        billerName: String,
        crnType: CRNType = CRNType.FIXED,
        completion: OnFrolloSDKCompletionListener<Resource<Long>>? = null
    ) {
        val request = ContactCreateUpdateRequest(
            name = name ?: nickName,
            nickName = nickName,
            description = description,
            paymentMethod = PaymentMethod.BPAY,
            paymentDetails = PaymentDetails.Biller(
                billerCode = billerCode,
                crn = crn,
                billerName = billerName,
                crnType = crnType
            )
        )
        createContact(request, completion)
    }

    /**
     * Create a new PayID contact on the host
     *
     * @param name Name of the contact; default value will be nickName (Optional)
     * @param nickName Nickname of the contact
     * @param description Description of the contact (Optional)
     * @param payId PayID value of the contact
     * @param payIdName Name of the PayID contact
     * @param payIdType Type of PayID; e.g. phone, email, abn, organisation id. Refer to [PayIDType]
     * @param completion Optional completion handler with optional error if the request fails else ID of the Contact created if success
     */
    fun createPayIDContact(
        name: String? = null,
        nickName: String,
        description: String? = null,
        payId: String,
        payIdName: String,
        payIdType: PayIDType,
        completion: OnFrolloSDKCompletionListener<Resource<Long>>? = null
    ) {
        val request = ContactCreateUpdateRequest(
            name = name ?: nickName,
            nickName = nickName,
            description = description,
            paymentMethod = PaymentMethod.PAY_ID,
            paymentDetails = PaymentDetails.PayID(
                payId = payId,
                name = payIdName,
                type = payIdType
            )
        )
        createContact(request, completion)
    }

    /**
     * Create a new International contact on the host
     *
     * @param name Name of the contact; default value will be nickName (Optional)
     * @param nickName Nickname of the contact
     * @param description Description of the contact (Optional)
     * @param country Country of the contact
     * @param message Message of the contact (optional)
     * @param bankCountry Country of the contact's bank
     * @param accountNumber Account number of the contact
     * @param bankAddress Bank Address of the contact (optional)
     * @param bic BIC of the contact's bank (optional)
     * @param fedwireNumber Fedwire number of the contact's bank (optional)
     * @param sortCode Sort code of the contact's bank (optional)
     * @param chipNumber Chip number of the contact's bank (optional)
     * @param routingNumber Routing number of the contact's bank (optional)
     * @param legalEntityIdentifier Legal entity identifier of the contact's bank (optional)
     * @param completion Optional completion handler with optional error if the request fails else ID of the Contact created if success
     */
    fun createInternationalContact(
        name: String? = null,
        nickName: String,
        description: String? = null,
        country: String,
        message: String? = null,
        bankCountry: String,
        accountNumber: String,
        bankAddress: String? = null,
        bic: String? = null,
        fedwireNumber: String? = null,
        sortCode: String? = null,
        chipNumber: String? = null,
        routingNumber: String? = null,
        legalEntityIdentifier: String? = null,
        completion: OnFrolloSDKCompletionListener<Resource<Long>>? = null
    ) {
        val request = ContactInternationalCreateUpdateRequest(
            name = name ?: nickName,
            nickName = nickName,
            description = description,
            paymentMethod = PaymentMethod.INTERNATIONAL,
            paymentDetails = ContactInternationalCreateUpdateRequest.InternationalPaymentDetails(
                name = name,
                country = country,
                message = message,
                bankCountry = bankCountry,
                accountNumber = accountNumber,
                bankAddress = bankAddress,
                bic = bic,
                fedWireNumber = fedwireNumber,
                sortCode = sortCode,
                chipNumber = chipNumber,
                routingNumber = routingNumber,
                legalEntityIdentifier = legalEntityIdentifier
            )
        )
        contactsAPI.createInternationalContact(request).enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#createContact.${request.paymentMethod.name}", resource.error?.localizedDescription)
                    completion?.invoke(Resource.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    handleContactResponse(response = resource.data, completionWithData = completion)
                }
            }
        }
    }

    private fun createContact(
        request: ContactCreateUpdateRequest,
        completion: OnFrolloSDKCompletionListener<Resource<Long>>? = null
    ) {
        contactsAPI.createContact(request).enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#createContact.${request.paymentMethod.name}", resource.error?.localizedDescription)
                    completion?.invoke(Resource.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    handleContactResponse(response = resource.data, completionWithData = completion)
                }
            }
        }
    }

    /**
     * Update a PayAnyone contact on the host
     *
     * @param contactId ID of the contact to be updated
     * @param name Name of the contact; default value will be nickName (Optional)
     * @param nickName Nickname of the contact
     * @param description Description of the contact (Optional)
     * @param accountName Account name of the payAnyone contact
     * @param bsb BSB of the payAnyone contact
     * @param accountNumber Account number of the payAnyone contact
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun updatePayAnyoneContact(
        contactId: Long,
        name: String? = null,
        nickName: String,
        description: String? = null,
        accountName: String,
        bsb: String,
        accountNumber: String,
        completion: OnFrolloSDKCompletionListener<Result>? = null
    ) {
        val request = ContactCreateUpdateRequest(
            name = name ?: nickName,
            nickName = nickName,
            description = description,
            paymentMethod = PaymentMethod.PAY_ANYONE,
            paymentDetails = PaymentDetails.PayAnyone(
                accountHolder = accountName,
                bsb = bsb,
                accountNumber = accountNumber
            )
        )
        updateContact(contactId, request, completion)
    }

    /**
     * Update a BPay contact on the host
     *
     * @param contactId ID of the contact to be updated
     * @param name Name of the contact; default value will be nickName (Optional)
     * @param nickName Nickname of the contact
     * @param description Description of the contact (Optional)
     * @param billerCode Biller Code of the BPAY contact
     * @param crn CRN of the BPAY contact
     * @param billerName Biller Name of the BPAY contact
     * @param crnType Type of the Biller's CRN; defaulted to Fixed CRN. refer to [CRNType]
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun updateBPayContact(
        contactId: Long,
        name: String? = null,
        nickName: String,
        description: String? = null,
        billerCode: String,
        crn: String,
        billerName: String,
        crnType: CRNType = CRNType.FIXED,
        completion: OnFrolloSDKCompletionListener<Result>? = null
    ) {
        val request = ContactCreateUpdateRequest(
            name = name ?: nickName,
            nickName = nickName,
            description = description,
            paymentMethod = PaymentMethod.BPAY,
            paymentDetails = PaymentDetails.Biller(
                billerCode = billerCode,
                crn = crn,
                billerName = billerName,
                crnType = crnType
            )
        )
        updateContact(contactId, request, completion)
    }

    /**
     * Update a PayID contact on the host
     *
     * @param contactId ID of the contact to be updated
     * @param name Name of the contact; default value will be nickName (Optional)
     * @param nickName Nickname of the contact
     * @param description Description of the contact (Optional)
     * @param payId PayID value of the contact
     * @param payIdName Name of the PayID contact
     * @param payIdType Type of PayID; e.g. phone, email, abn, organisation id. Refer to [PayIDType]
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun updatePayIDContact(
        contactId: Long,
        name: String? = null,
        nickName: String,
        description: String? = null,
        payId: String,
        payIdName: String,
        payIdType: PayIDType,
        completion: OnFrolloSDKCompletionListener<Result>? = null
    ) {
        val request = ContactCreateUpdateRequest(
            name = name ?: nickName,
            nickName = nickName,
            description = description,
            paymentMethod = PaymentMethod.PAY_ID,
            paymentDetails = PaymentDetails.PayID(
                payId = payId,
                name = payIdName,
                type = payIdType
            )
        )
        updateContact(contactId, request, completion)
    }

    /**
     * Update an International contact on the host
     *
     * @param contactId ID of the contact to be updated
     * @param name Name of the contact; default value will be nickName (Optional)
     * @param nickName Nickname of the contact
     * @param description Description of the contact (Optional)
     * @param country Country of the contact
     * @param message Message of the contact (optional)
     * @param bankCountry Country of the contact's bank
     * @param accountNumber Account number of the contact
     * @param bankAddress Bank Address of the contact (optional)
     * @param bic BIC of the contact's bank (optional)
     * @param fedwireNumber Fedwire number of the contact's bank (optional)
     * @param sortCode Sort code of the contact's bank (optional)
     * @param chipNumber Chip number of the contact's bank (optional)
     * @param routingNumber Routing number of the contact's bank (optional)
     * @param legalEntityIdentifier Legal entity identifier of the contact's bank (optional)
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun updateInternationalContact(
        contactId: Long,
        name: String? = null,
        nickName: String,
        description: String? = null,
        country: String,
        message: String? = null,
        bankCountry: String,
        accountNumber: String,
        bankAddress: String? = null,
        bic: String? = null,
        fedwireNumber: String? = null,
        sortCode: String? = null,
        chipNumber: String? = null,
        routingNumber: String? = null,
        legalEntityIdentifier: String? = null,
        completion: OnFrolloSDKCompletionListener<Result>? = null
    ) {
        val request = ContactInternationalCreateUpdateRequest(
            name = name ?: nickName,
            nickName = nickName,
            description = description,
            paymentMethod = PaymentMethod.INTERNATIONAL,
            paymentDetails = ContactInternationalCreateUpdateRequest.InternationalPaymentDetails(
                name = name,
                country = country,
                message = message,
                bankCountry = bankCountry,
                accountNumber = accountNumber,
                bankAddress = bankAddress,
                bic = bic,
                fedWireNumber = fedwireNumber,
                sortCode = sortCode,
                chipNumber = chipNumber,
                routingNumber = routingNumber,
                legalEntityIdentifier = legalEntityIdentifier
            )
        )
        contactsAPI.updateInternationalContact(contactId, request).enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#updateContact.${request.paymentMethod.name}", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    handleContactResponse(response = resource.data, completion = completion)
                }
            }
        }
    }

    private fun updateContact(
        contactId: Long,
        request: ContactCreateUpdateRequest,
        completion: OnFrolloSDKCompletionListener<Result>? = null
    ) {
        contactsAPI.updateContact(contactId, request).enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#updateContact.${request.paymentMethod.name}", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    handleContactResponse(response = resource.data, completion = completion)
                }
            }
        }
    }

    /**
     * Delete a specific contact by ID from the host
     *
     * @param contactId ID of the contact to be deleted
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun deleteContact(contactId: Long, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        contactsAPI.deleteContact(contactId).enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#deleteContact", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    db.contacts().deleteMany(longArrayOf(contactId))
                    completion?.invoke(Result.success())
                }
            }
        }
    }

    // Response Handlers

    private fun handleContactResponse(
        response: ContactResponse?,
        completion: OnFrolloSDKCompletionListener<Result>? = null,
        completionWithData: OnFrolloSDKCompletionListener<Resource<Long>>? = null
    ) {
        response?.let {
            doAsync {
                val model = response.toContact()

                db.contacts().insert(model)

                uiThread {
                    completion?.invoke(Result.success())
                    completionWithData?.invoke(Resource.success(response.contactId))
                }
            }
        } ?: run {
            completion?.invoke(Result.success())
            completionWithData?.invoke(Resource.success(null))
        } // Explicitly invoke completion callback if response is null.
    }

    private fun handleContactsWithPaginationResponse(
        response: List<ContactResponse>?,
        paymentMethod: PaymentMethod?,
        after: Long?,
        before: Long?,
        completion: OnFrolloSDKCompletionListener<PaginatedResult<PaginationInfo>>?
    ) {
        response?.let {
            doAsync {
                // Insert all contacts from API response
                val models = response.map { it.toContact() }
                db.contacts().insertAll(*models.toTypedArray())

                // Fetch IDs from API response
                val apiIds = response.map { it.contactId }.toHashSet()

                // Get IDs from database
                val contactIds = db.contacts().getIdsByQuery(
                    sqlForContactIdsToGetStaleIds(
                        before = before,
                        after = after,
                        paymentMethod = paymentMethod
                    )
                ).toHashSet()

                // Get stale IDs that are not present in the API response
                val staleIds = contactIds.minus(apiIds)

                // Delete the entries for these stale IDs from database if they exist
                if (staleIds.isNotEmpty()) {
                    db.contacts().deleteMany(staleIds.toLongArray())
                }

                uiThread {
                    val paginationInfo = PaginationInfo(before = before, after = after)
                    completion?.invoke(PaginatedResult.Success(paginationInfo))
                }
            }
        } ?: run { completion?.invoke(PaginatedResult.Success()) } // Explicitly invoke completion callback if response is null.
    }
}
