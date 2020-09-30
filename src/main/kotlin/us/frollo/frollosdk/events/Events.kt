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

package us.frollo.frollosdk.events

import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.base.Result
import us.frollo.frollosdk.core.ACTION.ACTION_BUDGET_CURRENT_PERIOD_READY
import us.frollo.frollosdk.core.ACTION.ACTION_REFRESH_TRANSACTIONS
import us.frollo.frollosdk.core.ARGUMENT.ARG_TRANSACTION_IDS
import us.frollo.frollosdk.core.OnFrolloSDKCompletionListener
import us.frollo.frollosdk.error.FrolloSDKError
import us.frollo.frollosdk.extensions.enqueue
import us.frollo.frollosdk.extensions.notify
import us.frollo.frollosdk.logging.Log
import us.frollo.frollosdk.model.api.events.EventCreateRequest
import us.frollo.frollosdk.model.coredata.notifications.NotificationPayload
import us.frollo.frollosdk.network.NetworkService
import us.frollo.frollosdk.network.api.EventsAPI

/**
 * Manages triggering and handling of events from the host
 */
class Events(network: NetworkService) {

    companion object {
        private const val TAG = "Events"
    }

    private val eventsAPI: EventsAPI = network.create(EventsAPI::class.java)

    /**
     * Trigger an event to occur on the host
     *
     * @param eventName Name of the event to trigger. Unrecognised ones will be ignored by the host
     * @param delayMinutes Delay in minutes for the host to delay the event (optional)
     * @param completion Completion handler with option error if something occurs (optional)
     */
    fun triggerEvent(eventName: String, delayMinutes: Long? = null, completion: OnFrolloSDKCompletionListener<Result>? = null) {
        eventsAPI.createEvent(EventCreateRequest(event = eventName, delayMinutes = delayMinutes ?: 0)).enqueue { resource ->
            when (resource.status) {
                Resource.Status.SUCCESS -> {
                    completion?.invoke(Result.success())
                }
                Resource.Status.ERROR -> {
                    Log.e("$TAG#triggerEvent", resource.error?.localizedDescription)
                    completion?.invoke(Result.error(resource.error))
                }
            }
        }
    }

    /**
     * Handle an event internally in case it triggers an actions
     *
     * @param eventName Name of the event to be handled. Unrecognised ones will be ignored
     * @param notificationPayload Payload of the associated notification (optional)
     * @param completion Completion handler indicating if the event was handled and any error that may have occurred (optional)
     */
    internal fun handleEvent(eventName: String, notificationPayload: NotificationPayload? = null, completion: ((handled: Boolean, error: FrolloSDKError?) -> Unit)? = null) {
        when (eventName) {
            EventNames.TEST.toString() -> {
                Log.i("$TAG#handleEvent", "Test event received")
                completion?.invoke(true, null)
            }
            EventNames.TRANSACTIONS_UPDATED.toString() -> {
                Log.i("$TAG#handleEvent", "Transactions updated event received")

                notificationPayload?.transactionIDs?.let {
                    notify(
                        action = ACTION_REFRESH_TRANSACTIONS,
                        extrasKey = ARG_TRANSACTION_IDS,
                        extrasData = it.toLongArray()
                    )
                }

                completion?.invoke(true, null)
            }
            EventNames.BUDGET_CURRENT_PERIOD_READY.toString() -> {
                Log.i("$TAG#handleEvent", "Current budget period ready event received")
                notify(action = ACTION_BUDGET_CURRENT_PERIOD_READY)
                completion?.invoke(true, null)
            }
            else -> {
                // Event not recognised
                completion?.invoke(false, null)
            }
        }
    }
}
