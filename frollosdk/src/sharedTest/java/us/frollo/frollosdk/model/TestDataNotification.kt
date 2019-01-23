package us.frollo.frollosdk.model

import androidx.core.os.bundleOf
import us.frollo.frollosdk.model.coredata.NotificationPayload

internal fun testMessageNotificationPayload() =
        NotificationPayload(
            event = "TEST_MESSAGE",
            link = "frollo://dashboard",
            transactionIDs = null,
            userMessageID = 12345L,
            userEventID = 98765L)

internal fun testEventNotificationBundle() =
        bundleOf(
                Pair("event", "TEST_EVENT"),
                Pair("user_event_id", "1234"))

internal fun testMessageNotificationBundle() =
        bundleOf(
                Pair("event", "TEST_MESSAGE"),
                Pair("link", "frollo://dashboard"),
                Pair("user_event_id", "98765"),
                Pair("user_message_id", "12345"))