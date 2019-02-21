package us.frollo.frollosdk.events

import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.base.Result
import us.frollo.frollosdk.core.OnFrolloSDKCompletionListener
import us.frollo.frollosdk.network.NetworkService
import us.frollo.frollosdk.network.api.EventsAPI
import us.frollo.frollosdk.error.FrolloSDKError
import us.frollo.frollosdk.extensions.enqueue
import us.frollo.frollosdk.logging.Log
import us.frollo.frollosdk.model.api.events.EventCreateRequest
import us.frollo.frollosdk.model.coredata.notifications.NotificationPayload

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
    fun triggerEvent(eventName: String, delayMinutes: Long? = null, completion: OnFrolloSDKCompletionListener? = null) {
        eventsAPI.createEvent(EventCreateRequest(event = eventName, delayMinutes = delayMinutes ?: 0)).enqueue { result ->
            when(result.status) {
                Resource.Status.SUCCESS -> {
                    completion?.invoke(Result.success())
                }
                Resource.Status.ERROR -> {
                    Log.e("$TAG#triggerEvent", result.error?.localizedDescription)
                    completion?.invoke(Result.error(result.error))
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
            else -> {
                // Event not recognised
                completion?.invoke(false, null)
            }
        }
    }
}