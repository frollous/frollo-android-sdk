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

package us.frollo.frollosdk.extensions

import android.os.Bundle
import androidx.core.os.bundleOf
import org.junit.Assert
import org.junit.Test

class ModelExtensionAndroidTest {

    @Test
    fun testBundleToNotificationPayload() {
        val bundle = bundleOf(
            Pair("event", "TEST_EVENT"),
            Pair("link", "frollo://"),
            Pair("transaction_ids", "[1,2,3]"),
            Pair("user_event_id", "1234"),
            Pair("user_message_id", "5678")
        )

        val payload = bundle.toNotificationPayload()

        Assert.assertEquals("TEST_EVENT", payload.event)
        Assert.assertEquals("frollo://", payload.link)
        Assert.assertEquals(1L, payload.transactionIDs?.get(0))
        Assert.assertEquals(2L, payload.transactionIDs?.get(1))
        Assert.assertEquals(3L, payload.transactionIDs?.get(2))
        Assert.assertEquals(1234L, payload.userEventID)
        Assert.assertEquals(5678L, payload.userMessageID)
    }

    @Test
    fun testMapToNotificationPayload() {
        val map = mutableMapOf(
            Pair("event", "TEST_EVENT"),
            Pair("link", "frollo://"),
            Pair("transaction_ids", "[1,2,3]"),
            Pair("user_event_id", "1234"),
            Pair("user_message_id", "5678")
        )

        val payload = map.toNotificationPayload()

        Assert.assertEquals("TEST_EVENT", payload.event)
        Assert.assertEquals("frollo://", payload.link)
        Assert.assertEquals(1L, payload.transactionIDs?.get(0))
        Assert.assertEquals(2L, payload.transactionIDs?.get(1))
        Assert.assertEquals(3L, payload.transactionIDs?.get(2))
        Assert.assertEquals(1234L, payload.userEventID)
        Assert.assertEquals(5678L, payload.userMessageID)
    }

    @Test
    fun testBundleToNotificationPayloadEmptyBundle() {
        val bundle = Bundle()

        val payload = bundle.toNotificationPayload()

        Assert.assertNull(payload.event)
        Assert.assertNull(payload.link)
        Assert.assertNull(payload.transactionIDs)
        Assert.assertNull(payload.userEventID)
        Assert.assertNull(payload.userMessageID)
    }
}
