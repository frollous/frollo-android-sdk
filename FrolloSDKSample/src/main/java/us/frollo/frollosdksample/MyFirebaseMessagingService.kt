package us.frollo.frollosdksample

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import us.frollo.frollosdk.FrolloSDK

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "MyFirebaseMessagingService"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        remoteMessage?.data?.let { data ->
            if (data.isNotEmpty()) {
                FrolloSDK.notifications.handlePushNotification(data)
            }

            Log.d(TAG,"**** Woohooo!! Received Notification!!")
        }
    }

    override fun onNewToken(token: String?) {
        Log.d(TAG,"**** New FCM token: $token")
        token?.let { FrolloSDK.notifications.registerPushNotificationToken(it) }
    }
}