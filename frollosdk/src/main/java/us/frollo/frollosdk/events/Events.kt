package us.frollo.frollosdk.events

import timber.log.Timber
import us.frollo.frollosdk.core.OnFrolloSDKCompletionListener
import us.frollo.frollosdk.data.remote.NetworkService
import us.frollo.frollosdk.data.remote.api.EventsAPI
import us.frollo.frollosdk.error.FrolloSDKError
import us.frollo.frollosdk.extensions.enqueue
import us.frollo.frollosdk.model.api.events.EventCreateRequest
import us.frollo.frollosdk.model.coredata.NotificationPayload

class Events(network: NetworkService) {

    private val eventsAPI: EventsAPI = network.create(EventsAPI::class.java)

    fun triggerEvent(eventName: String, delayMinutes: Long? = null, completion: OnFrolloSDKCompletionListener? = null) {
        eventsAPI.createEvent(EventCreateRequest(event = eventName, delayMinutes = delayMinutes ?: 0)).enqueue { _, error ->
            if (error != null)
                Timber.d(error.localizedDescription)

            completion?.invoke(error)
        }
    }

    internal fun handleEvent(eventName: String, notificationPayload: NotificationPayload? = null, completion: ((handled: Boolean, error: FrolloSDKError?) -> Unit)? = null) {
        when (eventName) {
            EventNames.TEST.toString() -> {
                Timber.d("Test event received")
                completion?.invoke(true, null)
            }
            else -> {
                // Event not recognised
                completion?.invoke(false, null)
            }
        }
    }
}