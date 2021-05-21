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

package us.frollo.frollosdk.paydays

import androidx.lifecycle.LiveData
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.base.Result
import us.frollo.frollosdk.core.OnFrolloSDKCompletionListener
import us.frollo.frollosdk.database.SDKDatabase
import us.frollo.frollosdk.extensions.enqueue
import us.frollo.frollosdk.logging.Log
import us.frollo.frollosdk.mapping.toPayday
import us.frollo.frollosdk.model.api.payday.PaydayResponse
import us.frollo.frollosdk.model.api.payday.PaydayUpdateRequest
import us.frollo.frollosdk.model.coredata.payday.Payday
import us.frollo.frollosdk.model.coredata.payday.PaydayFrequency
import us.frollo.frollosdk.network.NetworkService
import us.frollo.frollosdk.network.api.PaydayAPI

/** Manages user payday */
class Paydays(network: NetworkService, internal val db: SDKDatabase) {

    companion object {
        private const val TAG = "Paydays"
    }

    private val paydayAPI: PaydayAPI = network.create(PaydayAPI::class.java)

    // Payday

    /**
     * Fetch payday from the cache
     *
     * @return LiveData object of Resource<Payday> which can be observed using an Observer for future changes as well.
     */
    fun fetchPayday(): LiveData<Payday?> {
        return db.payday().loadLiveData()
    }

    /**
     * Refresh a payday from the host
     *
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun refreshPayday(completion: OnFrolloSDKCompletionListener<Result>? = null) {
        paydayAPI.fetchPayday().enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#refreshPayday", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    handlePaydayResponse(response = resource.data, completion = completion)
                }
            }
        }
    }

    /**
     * Update payday on the host
     *
     * @param frequency: Period the user is paid over
     * @param nextDate: Next date in the format yyyy-MM-dd the user is paid. See [Payday.DATE_FORMAT_PATTERN]
     * @param completion Optional completion handler with optional error if the request fails
     */
    fun updatePayday(
        frequency: PaydayFrequency? = null,
        nextDate: String? = null,
        completion: OnFrolloSDKCompletionListener<Result>? = null
    ) {
        val request = PaydayUpdateRequest(frequency = frequency, nextDate = nextDate)

        paydayAPI.updatePayday(request).enqueue { resource ->
            when (resource.status) {
                Resource.Status.ERROR -> {
                    Log.e("$TAG#updatePayday", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
                Resource.Status.SUCCESS -> {
                    handlePaydayResponse(response = resource.data, completion = completion)
                }
            }
        }
    }

    // Response Handlers

    private fun handlePaydayResponse(
        response: PaydayResponse?,
        completion: OnFrolloSDKCompletionListener<Result>? = null
    ) {
        response?.let {
            doAsync {
                val model = response.toPayday()
                val cachedModel = db.payday().load()
                cachedModel?.let {
                    model.paydayId = it.paydayId
                    db.payday().update(model)
                } ?: run {
                    db.payday().insert(model)
                }

                uiThread {
                    completion?.invoke(Result.success())
                }
            }
        } ?: run {
            completion?.invoke(Result.success())
        } // Explicitly invoke completion callback if response is null.
    }
}
