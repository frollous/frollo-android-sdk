package us.frollo.frollosdk.notifications

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.extensions.serializedName


internal enum class NotificationPayloadNames {
    @SerializedName("event") EVENT,
    @SerializedName("link") LINK,
    @SerializedName("transaction_ids") TRANSACTION_IDS,
    @SerializedName("user_event_id") USER_EVENT_ID,
    @SerializedName("user_message_id") USER_MESSAGE_ID;

    //This override MUST be used for this enum to work with Retrofit @Path or @Query parameters
    override fun toString(): String =
    //Try to get the annotation value if available instead of using plain .toString()
    //Fallback to super.toString() in case annotation is not present/available
            serializedName() ?: super.toString()
}