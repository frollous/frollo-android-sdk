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
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import us.frollo.frollosdk.aggregation.Aggregation
import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.base.Result
import us.frollo.frollosdk.core.OnFrolloSDKCompletionListener
import us.frollo.frollosdk.database.SDKDatabase
import us.frollo.frollosdk.network.NetworkService
import us.frollo.frollosdk.extensions.*
import us.frollo.frollosdk.logging.Log
import us.frollo.frollosdk.mapping.toBill
import us.frollo.frollosdk.model.api.bills.BillCreateRequest
import us.frollo.frollosdk.model.api.bills.BillResponse
import us.frollo.frollosdk.model.api.bills.BillUpdateRequest
import us.frollo.frollosdk.model.api.bills.BillsResponse
import us.frollo.frollosdk.model.coredata.bills.Bill
import us.frollo.frollosdk.model.coredata.bills.BillFrequency
import us.frollo.frollosdk.model.coredata.bills.BillRelation
import us.frollo.frollosdk.network.api.BillsAPI
import java.math.BigDecimal

class Bills(network: NetworkService, private val db: SDKDatabase, private val aggregation: Aggregation) {

    companion object {
        private const val TAG = "Bills"
        private const val TRANSACTION_BATCH_SIZE = 200
    }

    private val billsAPI: BillsAPI = network.create(BillsAPI::class.java)

    // Bill

    fun fetchBill(billId: Long): LiveData<Resource<Bill>> =
            Transformations.map(db.bills().load(billId)) { model ->
                Resource.success(model)
            }

    fun fetchBills(): LiveData<Resource<List<Bill>>> =
            Transformations.map(db.bills().load()) { models ->
                Resource.success(models)
            }

    fun fetchBillWithRelation(billId: Long): LiveData<Resource<BillRelation>> =
            Transformations.map(db.bills().loadWithRelation(billId)) { model ->
                Resource.success(model)
            }

    fun fetchBillsWithRelation(): LiveData<Resource<List<BillRelation>>> =
            Transformations.map(db.bills().loadWithRelation()) { models ->
                Resource.success(models)
            }

    fun createBill(name: String, frequency: BillFrequency, nextPaymentDate: String,
                   transactionId: Long? = null, transactionCategoryId: Long? = null,
                   dueAmount: BigDecimal? = null, notes: String? = null,
                   completion: OnFrolloSDKCompletionListener<Result>? = null) {
        val request = BillCreateRequest(
                name = name,
                frequency = frequency,
                nextPaymentDate = nextPaymentDate,
                transactionId = transactionId,
                categoryId = transactionCategoryId,
                dueAmount = dueAmount,
                notes = notes)

        billsAPI.createBill(request).enqueue { resource ->
            when(resource.status) {
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

    fun deleteBill(billId: Long, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        billsAPI.deleteBill(billId).enqueue { resource ->
            when(resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#deleteBill", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    removeCachedBills(longArrayOf(billId))
                }
            }
        }
    }

    fun refreshBills(completion: OnFrolloSDKCompletionListener<Result>? = null) {
        billsAPI.fetchBills().enqueue { resource ->
            when(resource.status) {
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

    fun refreshBill(billId: Long, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        billsAPI.fetchBill(billId).enqueue { resource ->
            when(resource.status) {
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

    fun updateBill(bill: Bill, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        val request = BillUpdateRequest(
                name = bill.name,
                categoryId = bill.categoryId,
                billType = bill.billType,
                status = bill.status,
                frequency = bill.frequency,
                nextPaymentDate = bill.nextPaymentDate,
                dueAmount = bill.dueAmount,
                notes = bill.notes)

        billsAPI.updateBill(bill.billId, request).enqueue { resource ->
            when(resource.status) {
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

    private fun mapBillResponse(models: List<BillResponse>): List<Bill> =
            models.map { it.toBill() }.toList()

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