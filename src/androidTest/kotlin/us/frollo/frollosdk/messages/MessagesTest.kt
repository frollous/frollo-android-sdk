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

package us.frollo.frollosdk.messages

import com.jraska.livedata.test
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Test

import org.junit.Assert.assertTrue
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertEquals
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import us.frollo.frollosdk.BaseAndroidTest
import us.frollo.frollosdk.base.Result
import us.frollo.frollosdk.error.DataError
import us.frollo.frollosdk.error.DataErrorSubType
import us.frollo.frollosdk.error.DataErrorType
import us.frollo.frollosdk.network.api.MessagesAPI
import us.frollo.frollosdk.model.coredata.messages.ContentType
import us.frollo.frollosdk.model.coredata.messages.MessageHTML
import us.frollo.frollosdk.model.coredata.messages.MessageImage
import us.frollo.frollosdk.model.coredata.messages.MessageText
import us.frollo.frollosdk.model.coredata.messages.MessageVideo
import us.frollo.frollosdk.model.testMessageNotificationPayload
import us.frollo.frollosdk.model.testMessageResponseData
import us.frollo.frollosdk.test.R
import us.frollo.frollosdk.testutils.readStringFromJson
import us.frollo.frollosdk.testutils.trimmedPath
import us.frollo.frollosdk.testutils.wait
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class MessagesTest : BaseAndroidTest() {

    override fun initSetup() {
        super.initSetup()

        preferences.loggedIn = true
        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900
    }

    @Test
    fun testFetchMessageByID() {
        initSetup()

        val data = testMessageResponseData()
        val list = mutableListOf(testMessageResponseData(), data, testMessageResponseData())
        database.messages().insertAll(*list.toTypedArray())

        val testObserver = messages.fetchMessage(data.messageId).test()
        testObserver.awaitValue()
        assertNotNull(testObserver.value().data)
        assertEquals(data.messageId, testObserver.value().data?.messageId)

        tearDown()
    }

    @Test
    fun testFetchAllMessages() {
        initSetup()

        val data1 = testMessageResponseData(read = false)
        val data2 = testMessageResponseData(read = true)
        val data3 = testMessageResponseData(read = false)
        val data4 = testMessageResponseData(read = true)
        val list = mutableListOf(data1, data2, data3, data4)

        database.messages().insertAll(*list.toTypedArray())

        val testObserver = messages.fetchMessages().test()
        testObserver.awaitValue()
        assertNotNull(testObserver.value().data)
        assertEquals(4, testObserver.value().data?.size)

        tearDown()
    }

    @Test
    fun testFetchUnreadMessages() {
        initSetup()

        val data1 = testMessageResponseData(read = false)
        val data2 = testMessageResponseData(read = true)
        val data3 = testMessageResponseData(read = false)
        val data4 = testMessageResponseData(read = true)
        val list = mutableListOf(data1, data2, data3, data4)

        database.messages().insertAll(*list.toTypedArray())

        val testObserver = messages.fetchMessages(read = false).test()
        testObserver.awaitValue()
        assertNotNull(testObserver.value().data)
        assertEquals(2, testObserver.value().data?.size)

        tearDown()
    }

    @Test
    fun testFetchMessagesByMessageType() {
        initSetup()

        val data1 = testMessageResponseData(types = mutableListOf("survey"))
        val data2 = testMessageResponseData(types = mutableListOf("event"))
        val data3 = testMessageResponseData(types = mutableListOf("survey", "welcome"))
        val data4 = testMessageResponseData(types = mutableListOf("dashboard_survey"))
        val list = mutableListOf(data1, data2, data3, data4)

        database.messages().insertAll(*list.toTypedArray())

        val testObserver = messages.fetchMessages(messageTypes = mutableListOf("survey")).test()
        testObserver.awaitValue()
        assertNotNull(testObserver.value().data)
        assertEquals(2, testObserver.value().data?.size)

        tearDown()
    }

    @Test
    fun testFetchMessagesByContentType() {
        initSetup()

        val data1 = testMessageResponseData(type = ContentType.TEXT, read = true)
        val data2 = testMessageResponseData(type = ContentType.TEXT, read = false)
        val data3 = testMessageResponseData(type = ContentType.VIDEO, read = false)
        val data4 = testMessageResponseData(type = ContentType.TEXT, read = false)
        val list = mutableListOf(data1, data2, data3, data4)

        database.messages().insertAll(*list.toTypedArray())

        val testObserver = messages.fetchMessages(read = false, contentType = ContentType.TEXT).test()
        testObserver.awaitValue()
        assertNotNull(testObserver.value().data)
        assertEquals(2, testObserver.value().data?.size)

        tearDown()
    }

    @Test
    fun testFetchMessagesCount() {
        initSetup()

        val data1 = testMessageResponseData(read = false)
        val data2 = testMessageResponseData(read = true)
        val data3 = testMessageResponseData(read = false)
        val data4 = testMessageResponseData(read = true)
        val list = mutableListOf(data1, data2, data3, data4)

        database.messages().insertAll(*list.toTypedArray())

        messages.fetchMessagesCount(read = true) { resource ->
            assertEquals(2L, resource.data)
        }

        tearDown()
    }

    @Test
    fun testRefreshMessages() {
        initSetup()

        val signal = CountDownLatch(1)

        val body = readStringFromJson(app, R.raw.messages_valid)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == MessagesAPI.URL_MESSAGES) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        messages.refreshMessages { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = messages.fetchMessages().test()
            testObserver.awaitValue()
            val models = testObserver.value().data
            assertNotNull(models)
            assertEquals(39, models?.size)
            models?.forEach { message ->
                when (message.contentType) {
                    ContentType.HTML -> assertTrue(message is MessageHTML)
                    ContentType.VIDEO -> assertTrue(message is MessageVideo)
                    ContentType.IMAGE -> assertTrue(message is MessageImage)
                    ContentType.TEXT -> assertTrue(message is MessageText)
                }
            }

            signal.countDown()
        }

        val request = mockServer.takeRequest()
        assertEquals(MessagesAPI.URL_MESSAGES, request.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testRefreshMessagesFailsIfLoggedOut() {
        initSetup()

        val signal = CountDownLatch(1)

        clearLoggedInPreferences()

        messages.refreshMessages { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.MISSING_ACCESS_TOKEN, (result.error as DataError).subType)

            signal.countDown()
        }

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testRefreshMessagesSkipsInvalid() {
        initSetup()

        val signal = CountDownLatch(1)

        val body = readStringFromJson(app, R.raw.messages_invalid)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == MessagesAPI.URL_MESSAGES) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        messages.refreshMessages { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = messages.fetchMessages().test()
            testObserver.awaitValue()
            val models = testObserver.value().data
            assertNotNull(models)
            assertEquals(35, models?.size)
            models?.forEach { message ->
                when (message.contentType) {
                    ContentType.HTML -> assertTrue(message is MessageHTML)
                    ContentType.VIDEO -> assertTrue(message is MessageVideo)
                    ContentType.IMAGE -> assertTrue(message is MessageImage)
                    ContentType.TEXT -> assertTrue(message is MessageText)
                }
            }

            signal.countDown()
        }

        val request = mockServer.takeRequest()
        assertEquals(MessagesAPI.URL_MESSAGES, request.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testRefreshMessageByID() {
        initSetup()

        val signal = CountDownLatch(1)

        val body = readStringFromJson(app, R.raw.message_id_12345)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == "messages/12345") {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        messages.refreshMessage(12345L) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = messages.fetchMessage(12345L).test()
            testObserver.awaitValue()
            val model = testObserver.value().data
            assertNotNull(model)
            assertEquals(12345L, model?.messageId)

            signal.countDown()
        }

        val request = mockServer.takeRequest()
        assertEquals("messages/12345", request.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testRefreshMessageByIDFailsIfLoggedOut() {
        initSetup()

        val signal = CountDownLatch(1)

        clearLoggedInPreferences()

        messages.refreshMessage(12345L) { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.MISSING_ACCESS_TOKEN, (result.error as DataError).subType)

            signal.countDown()
        }

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testRefreshUnreadMessages() {
        initSetup()

        val signal = CountDownLatch(1)

        val body = readStringFromJson(app, R.raw.messages_unread)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == MessagesAPI.URL_UNREAD) {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        messages.refreshUnreadMessages { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = messages.fetchMessages(read = false).test()
            testObserver.awaitValue()
            val models = testObserver.value().data
            assertNotNull(models)
            assertEquals(7, models?.size)
            models?.forEach { message ->
                when (message.contentType) {
                    ContentType.HTML -> assertTrue(message is MessageHTML)
                    ContentType.VIDEO -> assertTrue(message is MessageVideo)
                    ContentType.IMAGE -> assertTrue(message is MessageImage)
                    ContentType.TEXT -> assertTrue(message is MessageText)
                }
            }
            val metadata = models?.last()?.metadata
            assertEquals("holiday", metadata?.get("category")?.asString)
            assertEquals(true, metadata?.get("subcategory")?.asBoolean)

            signal.countDown()
        }

        val request = mockServer.takeRequest()
        assertEquals(MessagesAPI.URL_UNREAD, request.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testRefreshUnreadMessagesFailsIfLoggedOut() {
        initSetup()

        val signal = CountDownLatch(1)

        clearLoggedInPreferences()

        messages.refreshUnreadMessages { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.MISSING_ACCESS_TOKEN, (result.error as DataError).subType)

            signal.countDown()
        }

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testUpdateMessage() {
        initSetup()

        val signal = CountDownLatch(1)

        val body = readStringFromJson(app, R.raw.message_id_12345)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == "messages/12345") {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        messages.updateMessage(12345L, true, true) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = messages.fetchMessage(12345L).test()
            testObserver.awaitValue()
            val model = testObserver.value().data
            assertNotNull(model)
            assertEquals(12345L, model?.messageId)

            signal.countDown()
        }

        val request = mockServer.takeRequest()
        assertEquals("messages/12345", request.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testUpdateMessageFailsIfLoggedOut() {
        initSetup()

        val signal = CountDownLatch(1)

        clearLoggedInPreferences()

        messages.updateMessage(12345L, true, true) { result ->
            assertEquals(Result.Status.ERROR, result.status)
            assertNotNull(result.error)
            assertEquals(DataErrorType.AUTHENTICATION, (result.error as DataError).type)
            assertEquals(DataErrorSubType.MISSING_ACCESS_TOKEN, (result.error as DataError).subType)

            signal.countDown()
        }

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testHandlePushMessage() {
        initSetup()

        val body = readStringFromJson(app, R.raw.message_id_12345)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == "messages/12345") {
                    return MockResponse()
                            .setResponseCode(200)
                            .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        messages.handleMessageNotification(testMessageNotificationPayload())

        wait(1)

        val request = mockServer.takeRequest()
        assertEquals("messages/12345", request.trimmedPath)

        val testObserver = messages.fetchMessage(12345L).test()
        testObserver.awaitValue()
        val model = testObserver.value().data
        assertNotNull(model)
        assertEquals(12345L, model?.messageId)

        tearDown()
    }
}