package us.frollo.frollosdkapp

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import timber.log.Timber
import us.frollo.frollosdk.FrolloSDK

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        remoteMessage?.data?.let { data ->
            if (data.isNotEmpty()) {
                FrolloSDK.notifications.handlePushNotification(data)
            }

            Timber.d("**** Woohooo!! Received Notification!!")
        }
    }

    override fun onNewToken(token: String?) {
        Timber.d("**** New FCM token: $token")
        token?.let { FrolloSDK.notifications.registerPushNotificationToken(it) }
    }
}