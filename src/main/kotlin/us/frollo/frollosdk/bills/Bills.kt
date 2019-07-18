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

package us.frollo.frollosdk.bills

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.sqlite.db.SimpleSQLiteQuery
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import us.frollo.frollosdk.aggregation.Aggregation
import us.frollo.frollosdk.authentication.Authentication
import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.base.Result
import us.frollo.frollosdk.base.SimpleSQLiteQueryBuilder
import us.frollo.frollosdk.core.OnFrolloSDKCompletionListener
import us.frollo.frollosdk.database.SDKDatabase
import us.frollo.frollosdk.error.DataError
import us.frollo.frollosdk.error.DataErrorSubType
import us.frollo.frollosdk.error.DataErrorType
import us.frollo.frollosdk.extensions.enqueue
import us.frollo.frollosdk.extensions.fetchBillPayments
import us.frollo.frollosdk.extensions.sqlForBillPayments
import us.frollo.frollosdk.extensions.sqlForBills
import us.frollo.frollosdk.network.NetworkService
import us.frollo.frollosdk.logging.Log
import us.frollo.frollosdk.mapping.toBill
import us.frollo.frollosdk.mapping.toBillPayment
import us.frollo.frollosdk.model.api.bills.BillCreateRequest
import us.frollo.frollosdk.model.api.bills.BillPaymentRequestStatus
import us.frollo.frollosdk.model.api.bills.BillPaymentResponse
import us.frollo.frollosdk.model.api.bills.BillPaymentUpdateRequest
import us.frollo.frollosdk.model.api.bills.BillResponse
import us.frollo.frollosdk.model.api.bills.BillUpdateRequest
import us.frollo.frollosdk.model.api.bills.BillsResponse
import us.frollo.frollosdk.model.coredata.bills.Bill
import us.frollo.frollosdk.model.coredata.bills.BillFrequency
import us.frollo.frollosdk.model.coredata.bills.BillPayment
import us.frollo.frollosdk.model.coredata.bills.BillPaymentRelation
import us.frollo.frollosdk.model.coredata.bills.BillPaymentStatus
import us.frollo.frollosdk.model.coredata.bills.BillRelation
import us.frollo.frollosdk.model.coredata.bills.BillStatus
import us.frollo.frollosdk.model.coredata.bills.BillType
import us.frollo.frollosdk.network.api.BillsAPI
import java.math.BigDecimal

/** Manages bills and bill payments */
class Bills(network: NetworkService, private val db: SDKDatabase, private val aggregation: Aggregation, private val authentication: Authentication) {

    companion object {
        private const val TAG = "Bills"
    }

    private val billsAPI: BillsAPI = network.create(BillsAPI::class.java)

    // Bill

    /**
     * Fetch bill by ID from the cache
     *
     * @param billId Unique bill ID to fetch
     *
     * @return LiveData object of Resource<Bill> which can be observed using an Observer for future changes as well.
     */
    fun fetchBill(billId: Long): LiveData<Resource<Bill>> =
            Transformations.map(db.bills().load(billId)) { model ->
                Resource.success(model)
            }

    /**
     * Fetch bills from the cache
     *
     * @param frequency Filter by frequency of the bill payments (optional)
     * @param paymentStatus Filter by the payment status (optional)
     * @param status Filter by the status of the bill (optional)
     * @param type Filter by the type of the bill (optional)
     *
     * @return LiveData object of Resource<List<Bill> which can be observed using an Observer for future changes as well.
     */
    fun fetchBills(
        frequency: BillFrequency? = null,
        paymentStatus: BillPaymentStatus? = null,
        status: BillStatus? = null,
        type: BillType? = null
    ): LiveData<Resource<List<Bill>>> =
            Transformations.map(db.bills().loadByQuery(sqlForBills(frequency, paymentStatus, status, type))) { models ->
                Resource.success(models)
            }

    /**
     * Advanced method to fetch bills by SQL query from the cache
     *
     * @param query SimpleSQLiteQuery: Select query which fetches bills from the cache
     *
     * Note: Please check [SimpleSQLiteQueryBuilder] to build custom SQL queries
     *
     * @return LiveData object of Resource<List<Bill>> which can be observed using an Observer for future changes as well.
     */
    fun fetchBills(query: SimpleSQLiteQuery): LiveData<Resource<List<Bill>>> =
            Transformations.map(db.bills().loadByQuery(query)) { model ->
                Resource.success(model)
            }

    /**
     * Fetch bill by ID from the cache along with other associated data.
     *
     * @param billId Unique bill ID to fetch
     *
     * @return LiveData object of Resource<BillRelation> which can be observed using an Observer for future changes as well.
     */
    fun fetchBillWithRelation(billId: Long): LiveData<Resource<BillRelation>> =
            Transformations.map(db.bills().loadWithRelation(billId)) { model ->
                Resource.success(model)
            }

    /**
     * Fetch bills from the cache along with other associated data.
     *
     * @param frequency Filter by frequency of the bill payments (optional)
     * @param paymentStatus Filter by the payment status (optional)
     * @param status Filter by the status of the bill (optional)
     * @param type Filter by the type of the bill (optional)
     *
     * @return LiveData object of Resource<List<BillRelation> which can be observed using an Observer for future changes as well.
     */
    fun fetchBillsWithRelation(
        frequency: BillFrequency? = null,
        paymentStatus: BillPaymentStatus? = null,
        status: BillStatus? = null,
        type: BillType? = null
    ): LiveData<Resource<List<BillRelation>>> =
            Transformations.map(db.bills().loadByQueryWithRelation(sqlForBills(frequency, paymentStatus, status, type))) { models ->
                Resource.success(models)
            }

    /**
     * Advanced method to fetch bills by SQL query from the cache along with other associated data.
     *
     * @param query SimpleSQLiteQuery: Select query which fetches bills from the cache
     *
     * Note: Please check [SimpleSQLiteQueryBuilder] to build custom SQL queries
     *
     * @return LiveData object of Resource<List<BillRelation>> which can be observed using an Observer for future changes as well.
     */
    fun fetchBillsWithRelation(query: SimpleSQLiteQuery): LiveData<Resource<List<BillRelation>>> =
            Transformations.map(db.bills().loadByQueryWithRelation(query)) { model ->
                Resource.success(model)
            }

    /**
     * Create a new bill on the host from a transaction
     *
     * @param transactionId ID of the transaction representing a bill payment
     * @param frequency How often the bill recurrs
     * @param nextPaymentDate Date of the next payment is due
     * @param name Custom name for the bill (Optional: defaults to the transaction name)
     * @param notes Notes attached to the bill (Optional)
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun createBill(
        transactionId: Long,
        frequency: BillFrequency,
        nextPaymentDate: String,
        name: String? = null,
        notes: String? = null,
        completion: OnFrolloSDKCompletionListener<Result>? = null
    ) {
        val request = BillCreateRequest(
                transactionId = transactionId,
                dueAmount = null,
                name = name,
                frequency = frequency,
                nextPaymentDate = nextPaymentDate,
                notes = notes)

        createBill(request, completion)
    }

    /**
     * Create a new bill on the host manually
     *
     * @param dueAmount Amount the bill charges, recurring
     * @param frequency How often the bill recurrs
     * @param nextPaymentDate Date of the next payment is due
     * @param name Custom name for the bill
     * @param notes Notes attached to the bill (Optional)
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun createBill(
        dueAmount: BigDecimal,
        frequency: BillFrequency,
        nextPaymentDate: String,
        name: String,
        notes: String? = null,
        completion: OnFrolloSDKCompletionListener<Result>? = null
    ) {
        val request = BillCreateRequest(
                transactionId = null,
                dueAmount = dueAmount,
                name = name,
                frequency = frequency,
                nextPaymentDate = nextPaymentDate,
                notes = notes)

        createBill(request, completion)
    }

    private fun createBill(request: BillCreateRequest, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        if (!authentication.loggedIn) {
            val error = DataError(type = DataErrorType.AUTHENTICATION, subType = DataErrorSubType.LOGGED_OUT)
            Log.e("$TAG#createBill", error.localizedDescription)
            completion?.invoke(Result.error(error))
            return
        }

        billsAPI.createBill(request).enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#createBill", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    handleBillResponse(response = resource.data, completion = completion)
                }
            }
        }
    }

    /**
     * Delete a specific bill by ID from the host
     *
     * @param billId ID of the bill to be deleted
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun deleteBill(billId: Long, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        if (!authentication.loggedIn) {
            val error = DataError(type = DataErrorType.AUTHENTICATION, subType = DataErrorSubType.LOGGED_OUT)
            Log.e("$TAG#deleteBill", error.localizedDescription)
            completion?.invoke(Result.error(error))
            return
        }

        billsAPI.deleteBill(billId).enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#deleteBill", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    removeCachedBills(longArrayOf(billId))
                    completion?.invoke(Result.success())
                }
            }
        }
    }

    /**
     * Refresh all available bills from the host.
     *
     * Includes both estimated and confirmed bills.
     *
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun refreshBills(completion: OnFrolloSDKCompletionListener<Result>? = null) {
        if (!authentication.loggedIn) {
            val error = DataError(type = DataErrorType.AUTHENTICATION, subType = DataErrorSubType.LOGGED_OUT)
            Log.e("$TAG#refreshBills", error.localizedDescription)
            completion?.invoke(Result.error(error))
            return
        }

        billsAPI.fetchBills().enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#refreshBills", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    handleBillsResponse(response = resource.data, completion = completion)
                }
            }
        }
    }

    /**
     * Refresh a specific bill by ID from the host
     *
     * @param billId ID of the bill to fetch
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun refreshBill(billId: Long, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        if (!authentication.loggedIn) {
            val error = DataError(type = DataErrorType.AUTHENTICATION, subType = DataErrorSubType.LOGGED_OUT)
            Log.e("$TAG#refreshBill", error.localizedDescription)
            completion?.invoke(Result.error(error))
            return
        }

        billsAPI.fetchBill(billId).enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#refreshBill", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    handleBillResponse(response = resource.data, completion = completion)
                }
            }
        }
    }

    /**
     * Update a bill on the host
     *
     * @param bill Updated bill data model
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun updateBill(bill: Bill, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        if (!authentication.loggedIn) {
            val error = DataError(type = DataErrorType.AUTHENTICATION, subType = DataErrorSubType.LOGGED_OUT)
            Log.e("$TAG#updateBill", error.localizedDescription)
            completion?.invoke(Result.error(error))
            return
        }

        val request = BillUpdateRequest(
                name = bill.name,
                billType = bill.billType,
                status = bill.status,
                frequency = bill.frequency,
                nextPaymentDate = bill.nextPaymentDate,
                dueAmount = bill.dueAmount,
                notes = bill.notes)

        billsAPI.updateBill(bill.billId, request).enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#updateBill", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    handleBillResponse(response = resource.data, completion = completion)
                }
            }
        }
    }

    // Bill Payment

    /**
     * Fetch bill payment by ID from the cache
     *
     * @param billPaymentId Unique bill payment ID to fetch
     *
     * @return LiveData object of Resource<BillPayment> which can be observed using an Observer for future changes as well.
     */
    fun fetchBillPayment(billPaymentId: Long): LiveData<Resource<BillPayment>> =
            Transformations.map(db.billPayments().load(billPaymentId)) { model ->
                Resource.success(model)
            }

    /**
     * Fetch bill payments by bill ID from the cache
     *
     * @param billId Bill ID of the bill payments to fetch (optional)
     * @param fromDate Start date in the format yyyy-MM-dd to fetch bill payments from (inclusive) (optional). See [BillPayment.DATE_FORMAT_PATTERN]
     * @param toDate Start date in the format yyyy-MM-dd to fetch bill payments from (inclusive) (optional). See [BillPayment.DATE_FORMAT_PATTERN]
     * @param frequency Filter by frequency of the bill payments (optional)
     * @param paymentStatus Filter by the payment status (optional)
     *
     * @return LiveData object of Resource<List<BillPayment>> which can be observed using an Observer for future changes as well.
     */
    fun fetchBillPayments(
        billId: Long? = null,
        fromDate: String? = null,
        toDate: String? = null,
        frequency: BillFrequency? = null,
        paymentStatus: BillPaymentStatus? = null
    ): LiveData<Resource<List<BillPayment>>> =
            Transformations.map(db.billPayments().loadByQuery(sqlForBillPayments(billId, fromDate, toDate, frequency, paymentStatus))) { models ->
                Resource.success(models)
            }

    /**
     * Advanced method to fetch bill payments by SQL query from the cache
     *
     * @param query SimpleSQLiteQuery: Select query which fetches bill payments from the cache
     *
     * Note: Please check [SimpleSQLiteQueryBuilder] to build custom SQL queries
     *
     * @return LiveData object of Resource<List<BillPayment>> which can be observed using an Observer for future changes as well.
     */
    fun fetchBillPayments(query: SimpleSQLiteQuery): LiveData<Resource<List<BillPayment>>> =
            Transformations.map(db.billPayments().loadByQuery(query)) { model ->
                Resource.success(model)
            }

    /**
     * Fetch bill payment by ID from the cache with associated data
     *
     * @param billPaymentId Unique bill payment ID to fetch
     *
     * @return LiveData object of Resource<BillPaymentRelation> which can be observed using an Observer for future changes as well.
     */
    fun fetchBillPaymentWithRelation(billPaymentId: Long): LiveData<Resource<BillPaymentRelation>> =
            Transformations.map(db.billPayments().loadWithRelation(billPaymentId)) { model ->
                Resource.success(model)
            }

    /**
     * Fetch bill payments from the cache with associated data
     *
     * @param billId Bill ID of the bill payments to fetch (optional)
     * @param fromDate Start date in the format yyyy-MM-dd to fetch bill payments from (inclusive) (optional). See [BillPayment.DATE_FORMAT_PATTERN]
     * @param toDate Start date in the format yyyy-MM-dd to fetch bill payments from (inclusive) (optional). See [BillPayment.DATE_FORMAT_PATTERN]
     * @param frequency Filter by frequency of the bill payments (optional)
     * @param paymentStatus Filter by the payment status (optional)
     *
     * @return LiveData object of Resource<List<BillPaymentRelation>> which can be observed using an Observer for future changes as well.
     */
    fun fetchBillPaymentsWithRelation(
        billId: Long? = null,
        fromDate: String? = null,
        toDate: String? = null,
        frequency: BillFrequency? = null,
        paymentStatus: BillPaymentStatus? = null
    ): LiveData<Resource<List<BillPaymentRelation>>> =
            Transformations.map(db.billPayments().loadByQueryWithRelation(sqlForBillPayments(billId, fromDate, toDate, frequency, paymentStatus))) { models ->
                Resource.success(models)
            }

    /**
     * Advanced method to fetch bill payments by SQL query from the cache with associated data
     *
     * @param query SimpleSQLiteQuery: Select query which fetches bill payments from the cache
     *
     * Note: Please check [SimpleSQLiteQueryBuilder] to build custom SQL queries
     *
     * @return LiveData object of Resource<List<BillPaymentRelation>> which can be observed using an Observer for future changes as well.
     */
    fun fetchBillPaymentsWithRelation(query: SimpleSQLiteQuery): LiveData<Resource<List<BillPaymentRelation>>> =
            Transformations.map(db.billPayments().loadByQueryWithRelation(query)) { model ->
                Resource.success(model)
            }

    /**
     * Delete a specific bill payment by ID from the host
     *
     * @param billPaymentId ID of the bill payment to be deleted
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun deleteBillPayment(billPaymentId: Long, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        if (!authentication.loggedIn) {
            val error = DataError(type = DataErrorType.AUTHENTICATION, subType = DataErrorSubType.LOGGED_OUT)
            Log.e("$TAG#deleteBillPayment", error.localizedDescription)
            completion?.invoke(Result.error(error))
            return
        }

        billsAPI.deleteBillPayment(billPaymentId).enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#deleteBillPayment", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    removeCachedBillPayments(longArrayOf(billPaymentId))
                    completion?.invoke(Result.success())
                }
            }
        }
    }

    /**
     * Refresh bill payments from a certain period from the host
     *
     * @param fromDate Start date in the format yyyy-MM-dd to fetch bill payments from (inclusive). See [BillPayment.DATE_FORMAT_PATTERN]
     * @param toDate Start date in the format yyyy-MM-dd to fetch bill payments from (inclusive). See [BillPayment.DATE_FORMAT_PATTERN]
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun refreshBillPayments(fromDate: String, toDate: String, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        if (!authentication.loggedIn) {
            val error = DataError(type = DataErrorType.AUTHENTICATION, subType = DataErrorSubType.LOGGED_OUT)
            Log.e("$TAG#refreshBillPayments", error.localizedDescription)
            completion?.invoke(Result.error(error))
            return
        }

        billsAPI.fetchBillPayments(fromDate = fromDate, toDate = toDate).enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#refreshBillPayments", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    handleBillPaymentsResponse(response = resource.data, fromDate = fromDate, toDate = toDate, completion = completion)
                }
            }
        }
    }

    /**
     * Update a bill payment on the host
     *
     * @param billPaymentId ID of the bill payment to be updated
     * @param date Date of the bill payment (Optional)
     * @param paid Indicates if the bill payment to b marked as paid/unpaid. true if to be marked as paid. (Optional)
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun updateBillPayment(billPaymentId: Long, date: String? = null, paid: Boolean? = null, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        if (!authentication.loggedIn) {
            val error = DataError(type = DataErrorType.AUTHENTICATION, subType = DataErrorSubType.LOGGED_OUT)
            Log.e("$TAG#updateBillPayment", error.localizedDescription)
            completion?.invoke(Result.error(error))
            return
        }

        val status = paid?.let {
            if (paid) BillPaymentRequestStatus.PAID else BillPaymentRequestStatus.UNPAID
        }

        val request = BillPaymentUpdateRequest(date = date, status = status)

        billsAPI.updateBillPayment(billPaymentId, request).enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#updateBillPayment", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    handleBillPaymentResponse(response = resource.data, completion = completion)
                }
            }
        }
    }

    // Response Handlers

    private fun handleBillsResponse(response: BillsResponse?, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        response?.let {
            doAsync {
                val models = mapBillResponse(response.bills)

                aggregation.fetchMissingMerchants(models.mapNotNull { it.merchantId }.toSet())

                db.bills().insertAll(*models.toTypedArray())

                val apiIds = models.map { it.billId }.toList()
                val staleIds = db.bills().getStaleIds(apiIds.toLongArray())

                if (staleIds.isNotEmpty()) {
                    removeCachedBills(staleIds.toLongArray())
                }

                uiThread { completion?.invoke(Result.success()) }
            }
        } ?: run { completion?.invoke(Result.success()) } // Explicitly invoke completion callback if response is null.
    }

    private fun handleBillResponse(response: BillResponse?, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        response?.let {
            doAsync {
                val model = response.toBill()

                model.merchantId?.let { aggregation.fetchMissingMerchants(setOf(it)) }

                db.bills().insert(model)

                uiThread { completion?.invoke(Result.success()) }
            }
        } ?: run { completion?.invoke(Result.success()) } // Explicitly invoke completion callback if response is null.
    }

    private fun handleBillPaymentsResponse(
        response: List<BillPaymentResponse>?,
        fromDate: String,
        toDate: String,
        completion: OnFrolloSDKCompletionListener<Result>? = null
    ) {
        response?.let {
            doAsync {
                val models = mapBillPaymentResponse(response)

                aggregation.fetchMissingMerchants(models.mapNotNull { it.merchantId }.toSet())

                db.billPayments().insertAll(*models.toTypedArray())

                val apiIds = models.map { it.billPaymentId }.toHashSet()
                val allBillPaymentIds = db.billPayments().getIds(fromDate = fromDate, toDate = toDate).toHashSet()
                val staleIds = allBillPaymentIds.minus(apiIds)

                if (staleIds.isNotEmpty()) {
                    removeCachedBillPayments(staleIds.toLongArray())
                }

                uiThread { completion?.invoke(Result.success()) }
            }
        } ?: run { completion?.invoke(Result.success()) } // Explicitly invoke completion callback if response is null.
    }

    private fun handleBillPaymentResponse(response: BillPaymentResponse?, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        response?.let {
            doAsync {
                val model = response.toBillPayment()

                model.merchantId?.let { aggregation.fetchMissingMerchants(setOf(it)) }

                db.billPayments().insert(model)

                uiThread { completion?.invoke(Result.success()) }
            }
        } ?: run { completion?.invoke(Result.success()) } // Explicitly invoke completion callback if response is null.
    }

    private fun mapBillResponse(models: List<BillResponse>): List<Bill> =
            models.map { it.toBill() }.toList()

    private fun mapBillPaymentResponse(models: List<BillPaymentResponse>): List<BillPayment> =
            models.map { it.toBillPayment() }.toList()

    // WARNING: Do not call this method on the main thread
    private fun removeCachedBills(billIds: LongArray) {
        db.bills().deleteMany(billIds)

        // Manually delete bill payments associated to this bill
        // as we are not using ForeignKeys because ForeignKey constraints
        // do not allow to insert data into child table prior to parent table
        val billPaymentIds = db.billPayments().getIdsByBillIds(billIds)
        removeCachedBillPayments(billPaymentIds)
    }

    // WARNING: Do not call this method on the main thread
    private fun removeCachedBillPayments(billPaymentIds: LongArray) {
        db.billPayments().deleteMany(billPaymentIds)
    }
}