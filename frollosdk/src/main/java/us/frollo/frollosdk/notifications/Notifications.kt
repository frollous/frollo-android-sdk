package us.frollo.frollosdk.notifications

import android.os.Bundle
import timber.log.Timber
import us.frollo.frollosdk.auth.Authentication
import us.frollo.frollosdk.events.Events
import us.frollo.frollosdk.extensions.toNotificationPayload
import us.frollo.frollosdk.messages.Messages
import us.frollo.frollosdk.model.coredata.NotificationPayload
import java.lang.Exception

class Notifications(private val authentication: Authentication, private val events: Events, private val messages: Messages) {

    fun registerPushNotificationToken(token: String) {
        authentication.updateDevice(notificationToken = token)
    }

    fun handlePushNotification(data: Map<String, String>) {
        try {
            val notificationPayload = data.toNotificationPayload()

            handlePushNotification(notificationPayload)
        } catch (e: Exception) {
            Timber.d(e)
        }
    }

    fun handlePushNotification(bundle: Bundle) {
        try {
            val notificationPayload = bundle.toNotificationPayload()

            handlePushNotification(notificationPayload)
        } catch (e: Exception) {
            Timber.d(e)
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