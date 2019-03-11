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

package us.frollo.frollosdk.model

import androidx.core.os.bundleOf
import us.frollo.frollosdk.model.coredata.notifications.NotificationPayload

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

internal fun testTransactionUpdatedNotificationPayload() =
        NotificationPayload(
                event = "T_UPDATED",
                transactionIDs = listOf(45123, 986, 7000072),
                userEventID = 98765L)