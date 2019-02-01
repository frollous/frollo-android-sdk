package us.frollo.frollosdk.notifications

import android.os.Bundle
import us.frollo.frollosdk.auth.Authentication
import us.frollo.frollosdk.events.Events
import us.frollo.frollosdk.extensions.toNotificationPayload
import us.frollo.frollosdk.logging.Log
import us.frollo.frollosdk.messages.Messages
import us.frollo.frollosdk.model.coredata.notifications.NotificationPayload
import java.lang.Exception

/**
 * Register for push notifications and handles incoming push notification payloads.
 */
class Notifications(private val authentication: Authentication, private val events: Events, private val messages: Messages) {

    companion object {
        private const val TAG = "Notifications"
    }

    /**
     * Registers the device token received from Firebase to the host to allow for push notifications to be sent
     *
     * @param token Raw token data received from Firebase to be sent to the host
     */
    fun registerPushNotificationToken(token: String) {
        authentication.updateDevice(notificationToken = token)
    }

    /**
     * Handles the userInfo payload on a push notification. Any custom data or message content will be processed by the SDK.
     *
     * @param data Notification user info payload received from the push
     */
    fun handlePushNotification(data: Map<String, String>) {
        try {
            val notificationPayload = data.toNotificationPayload()

            handlePushNotification(notificationPayload)
        } catch (e: Exception) {
            Log.e("$TAG#handlePushNotification", e.message)
        }
    }

    /**
     * Handles the userInfo payload on a push notification. Any custom data or message content will be processed by the SDK.
     *
     * @param bundle Notification user info payload received from the push
     */
    fun handlePushNotification(bundle: Bundle) {
        try {
            val notificationPayload = bundle.toNotificationPayload()

            handlePushNotification(notificationPayload)
        } catch (e: Exception) {
            Log.e("$TAG#handlePushNotification", e.message)
        }
    }

    private fun handlePushNotification(notificationPayload: NotificationPayload) {
        notificationPayload.event?.let { event ->
            events.handleEvent(eventName = event, notificationPayload = notificationPayload)
        }

        notificationPayload.userMessageID?.let {
            messages.handleMessageNotification(notificationPayload)
        }
    }
}