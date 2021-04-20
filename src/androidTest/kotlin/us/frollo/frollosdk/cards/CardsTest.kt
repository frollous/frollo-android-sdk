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

package us.frollo.frollosdk.cards

import com.jraska.livedata.test
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import us.frollo.frollosdk.BaseAndroidTest
import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.base.Result
import us.frollo.frollosdk.error.DataError
import us.frollo.frollosdk.error.DataErrorSubType
import us.frollo.frollosdk.error.DataErrorType
import us.frollo.frollosdk.mapping.toAccount
import us.frollo.frollosdk.mapping.toCard
import us.frollo.frollosdk.model.coredata.cards.CardDesignType
import us.frollo.frollosdk.model.coredata.cards.CardIssuer
import us.frollo.frollosdk.model.coredata.cards.CardLockOrReplaceReason
import us.frollo.frollosdk.model.coredata.cards.CardStatus
import us.frollo.frollosdk.model.coredata.cards.CardType
import us.frollo.frollosdk.model.testAccountResponseData
import us.frollo.frollosdk.model.testCardResponseData
import us.frollo.frollosdk.network.api.CardsAPI
import us.frollo.frollosdk.test.R
import us.frollo.frollosdk.testutils.readStringFromJson
import us.frollo.frollosdk.testutils.trimmedPath
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class CardsTest : BaseAndroidTest() {

    override fun initSetup() {
        super.initSetup()

        preferences.loggedIn = true
        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900
    }

    @Test
    fun testFetchCardById() {
        initSetup()

        val data1 = testCardResponseData(cardId = 100)
        val data2 = testCardResponseData(cardId = 101)
        val data3 = testCardResponseData(cardId = 102)

        val list = mutableListOf(data1, data2, data3)

        database.cards().insertAll(*list.map { it.toCard() }.toTypedArray())

        val testObserver = cards.fetchCard(cardId = 101).test()

        testObserver.awaitValue()
        assertNotNull(testObserver.value())
        assertEquals(101L, testObserver.value()?.cardId)

        tearDown()
    }

    @Test
    fun testFetchCards() {
        initSetup()

        val data1 = testCardResponseData(cardId = 100, status = CardStatus.ACTIVE, accountId = 200)
        val data2 = testCardResponseData(cardId = 101, status = CardStatus.ACTIVE, accountId = 200)
        val data3 = testCardResponseData(cardId = 102, status = CardStatus.ACTIVE, accountId = 200)
        val data4 = testCardResponseData(cardId = 103, status = CardStatus.PENDING, accountId = 200)
        val data5 = testCardResponseData(cardId = 105, status = CardStatus.ACTIVE, accountId = 201)
        val data6 = testCardResponseData(cardId = 106, status = CardStatus.PENDING, accountId = 201)

        val list = mutableListOf(data1, data2, data3, data4, data5, data6)

        database.cards().insertAll(*list.map { it.toCard() }.toTypedArray())

        val testObserver = cards.fetchCards(accountId = 200, status = CardStatus.ACTIVE).test()

        testObserver.awaitValue()
        assertTrue(testObserver.value()?.isNotEmpty() == true)
        assertEquals(3, testObserver.value()?.size)

        tearDown()
    }

    @Test
    fun testRefreshCardById() {
        initSetup()

        val signal = CountDownLatch(1)

        val cardId: Long = 3

        val requestPath = "cards/$cardId"

        val body = readStringFromJson(app, R.raw.card_by_id)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == requestPath) {
                    return MockResponse()
                        .setResponseCode(200)
                        .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        cards.refreshCard(cardId = cardId) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = cards.fetchCard(cardId = cardId).test()

            testObserver.awaitValue()
            assertNotNull(testObserver.value())
            assertEquals(cardId, testObserver.value()?.cardId)
            assertEquals(5182L, testObserver.value()?.accountId)

            signal.countDown()
        }

        val request = mockServer.takeRequest()
        assertEquals(requestPath, request.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testRefreshCardByIdFailsIfLoggedOut() {
        initSetup()

        val signal = CountDownLatch(1)

        clearLoggedInPreferences()

        cards.refreshCard(cardId = 3) { result ->
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
    fun testRefreshCards() {
        initSetup()

        val signal = CountDownLatch(1)

        val body = readStringFromJson(app, R.raw.cards_get)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == CardsAPI.URL_CARDS) {
                    return MockResponse()
                        .setResponseCode(200)
                        .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        cards.refreshCards { resource ->
            assertEquals(Resource.Status.SUCCESS, resource.status)
            assertNull(resource.error)

            val testObserver = cards.fetchCards().test()

            testObserver.awaitValue()
            assertNotNull(testObserver.value())
            assertEquals(2, testObserver.value()?.size)
            assertEquals(123L, testObserver.value()?.first()?.cardId)
            assertEquals(542L, testObserver.value()?.first()?.accountId)
            assertEquals(CardStatus.ACTIVE, testObserver.value()?.first()?.status)
            assertEquals(CardType.DEBIT, testObserver.value()?.first()?.type)
            assertEquals(CardDesignType.DEFAULT, testObserver.value()?.first()?.designType)
            assertEquals("2020-12-01T18:25:43.511Z", testObserver.value()?.first()?.createdDate)
            assertEquals("2020-12-02T18:25:43.511Z", testObserver.value()?.first()?.cancelledDate)
            assertEquals("Everyday Debit Card", testObserver.value()?.first()?.name)
            assertEquals("Splurge Card", testObserver.value()?.first()?.nickName)
            assertEquals("1234", testObserver.value()?.first()?.panLastDigits)
            assertEquals("02/24", testObserver.value()?.first()?.expiryDate)
            assertEquals("Joe Blow", testObserver.value()?.first()?.cardholderName)
            assertEquals(CardIssuer.VISA, testObserver.value()?.first()?.issuer)

            signal.countDown()
        }

        val request = mockServer.takeRequest()
        assertEquals(CardsAPI.URL_CARDS, request.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testRefreshCardsFailsIfLoggedOut() {
        initSetup()

        val signal = CountDownLatch(1)

        clearLoggedInPreferences()

        cards.refreshCards { resource ->
            assertEquals(Resource.Status.ERROR, resource.status)
            assertNotNull(resource.error)
            assertEquals(DataErrorType.AUTHENTICATION, (resource.error as DataError).type)
            assertEquals(DataErrorSubType.MISSING_ACCESS_TOKEN, (resource.error as DataError).subType)

            signal.countDown()
        }

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testCreateCard() {
        initSetup()

        val signal = CountDownLatch(1)

        val body = readStringFromJson(app, R.raw.card_create)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == CardsAPI.URL_CARDS) {
                    return MockResponse()
                        .setResponseCode(200)
                        .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        cards.createCard(
            accountId = 325,
            firstName = "Jacob",
            lastName = "Smith",
            addressId = 123
        ) { resource ->
            assertEquals(Resource.Status.SUCCESS, resource.status)
            assertNull(resource.error)
            assertEquals(2L, resource.data)

            val testObserver = cards.fetchCard(cardId = 2).test()

            testObserver.awaitValue()
            assertNotNull(testObserver.value())
            assertEquals(2L, testObserver.value()?.cardId)
            assertEquals(CardStatus.PENDING, testObserver.value()?.status)

            signal.countDown()
        }

        val request = mockServer.takeRequest()
        assertEquals(CardsAPI.URL_CARDS, request.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testCreateCardFailsIfLoggedOut() {
        initSetup()

        val signal = CountDownLatch(1)

        clearLoggedInPreferences()

        cards.createCard(
            accountId = 325,
            firstName = "Jacob",
            lastName = "Smith",
            addressId = 123
        ) { resource ->
            assertEquals(Resource.Status.ERROR, resource.status)
            assertNotNull(resource.error)
            assertEquals(DataErrorType.AUTHENTICATION, (resource.error as DataError).type)
            assertEquals(DataErrorSubType.MISSING_ACCESS_TOKEN, (resource.error as DataError).subType)

            signal.countDown()
        }

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testUpdateCard() {
        initSetup()

        val signal = CountDownLatch(1)

        val cardId: Long = 3

        val requestPath = "cards/$cardId"

        val body = readStringFromJson(app, R.raw.card_by_id)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == requestPath) {
                    return MockResponse()
                        .setResponseCode(200)
                        .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        val card = testCardResponseData(cardId = cardId).toCard()

        database.cards().insert(card)

        cards.updateCard(card) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = cards.fetchCard(cardId = cardId).test()

            testObserver.awaitValue()
            val model = testObserver.value()
            assertNotNull(model)
            assertEquals(cardId, model?.cardId)
            assertEquals("My Card Name", model?.name)

            signal.countDown()
        }

        val request = mockServer.takeRequest()
        assertEquals(requestPath, request.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testUpdateCardFailsIfLoggedOut() {
        initSetup()

        val signal = CountDownLatch(1)

        clearLoggedInPreferences()

        val card = testCardResponseData(cardId = 3).toCard()
        cards.updateCard(card) { result ->
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
    fun testSetCardPin() {
        initSetup()

        val signal = CountDownLatch(1)

        val cardId = 1L
        val requestPath = "cards/$cardId/pin"

        val body = readStringFromJson(app, R.raw.card_set_pin)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == requestPath) {
                    return MockResponse()
                        .setResponseCode(200)
                        .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        val card = testCardResponseData(cardId = cardId, status = CardStatus.PENDING).toCard()

        database.cards().insert(card)

        cards.setCardPin(
            cardId = cardId,
            encryptedPIN = "100110 111010 001011 101001",
            keyId = "d79fe9eb-66dc-4929-bbe8-954d55222e15"
        ) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = cards.fetchCard(cardId = cardId).test()

            testObserver.awaitValue()
            assertNotNull(testObserver.value())
            assertEquals(cardId, testObserver.value()?.cardId)
            assertEquals(CardStatus.ACTIVE, testObserver.value()?.status)

            signal.countDown()
        }

        val request = mockServer.takeRequest()
        assertEquals(requestPath, request.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testSetCardPinFailsIfLoggedOut() {
        initSetup()

        val signal = CountDownLatch(1)

        clearLoggedInPreferences()

        cards.setCardPin(
            cardId = 1,
            encryptedPIN = "100110 111010 001011 101001",
            keyId = "d79fe9eb-66dc-4929-bbe8-954d55222e15"
        ) { result ->
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
    fun testActivateCard() {
        initSetup()

        val signal = CountDownLatch(1)

        val cardId = 2L
        val requestPath = "cards/$cardId/activate"

        val body = readStringFromJson(app, R.raw.card_activate)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == requestPath) {
                    return MockResponse()
                        .setResponseCode(200)
                        .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        val card = testCardResponseData(cardId = cardId, status = CardStatus.PENDING).toCard()

        database.cards().insert(card)

        cards.activateCard(cardId = cardId, panLastFourDigits = "1234") { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = cards.fetchCard(cardId = cardId).test()

            testObserver.awaitValue()
            assertNotNull(testObserver.value())
            assertEquals(cardId, testObserver.value()?.cardId)
            assertEquals(CardStatus.ACTIVE, testObserver.value()?.status)

            signal.countDown()
        }

        val request = mockServer.takeRequest()
        assertEquals(requestPath, request.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testActivateCardFailsIfLoggedOut() {
        initSetup()

        val signal = CountDownLatch(1)

        clearLoggedInPreferences()

        cards.activateCard(cardId = 2, panLastFourDigits = "1234") { result ->
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
    fun testLockCard() {
        initSetup()

        val signal = CountDownLatch(1)

        val cardId = 4L
        val requestPath = "cards/$cardId/lock"

        val body = readStringFromJson(app, R.raw.card_lock)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == requestPath) {
                    return MockResponse()
                        .setResponseCode(200)
                        .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        val card = testCardResponseData(cardId = cardId, status = CardStatus.ACTIVE).toCard()

        database.cards().insert(card)

        cards.lockCard(cardId = cardId, reason = CardLockOrReplaceReason.LOSS) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = cards.fetchCard(cardId = cardId).test()

            testObserver.awaitValue()
            assertNotNull(testObserver.value())
            assertEquals(cardId, testObserver.value()?.cardId)
            assertEquals(CardStatus.LOCKED, testObserver.value()?.status)

            signal.countDown()
        }

        val request = mockServer.takeRequest()
        assertEquals(requestPath, request.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testLockCardFailsIfLoggedOut() {
        initSetup()

        val signal = CountDownLatch(1)

        clearLoggedInPreferences()

        cards.lockCard(cardId = 4, reason = CardLockOrReplaceReason.LOSS) { result ->
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
    fun testUnlockCard() {
        initSetup()

        val signal = CountDownLatch(1)

        val cardId = 5L
        val requestPath = "cards/$cardId/unlock"

        val body = readStringFromJson(app, R.raw.card_unlock)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == requestPath) {
                    return MockResponse()
                        .setResponseCode(200)
                        .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        val card = testCardResponseData(cardId = cardId, status = CardStatus.LOCKED).toCard()

        database.cards().insert(card)

        cards.unlockCard(cardId = cardId) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = cards.fetchCard(cardId = cardId).test()

            testObserver.awaitValue()
            assertNotNull(testObserver.value())
            assertEquals(cardId, testObserver.value()?.cardId)
            assertEquals(CardStatus.ACTIVE, testObserver.value()?.status)

            signal.countDown()
        }

        val request = mockServer.takeRequest()
        assertEquals(requestPath, request.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testUnlockCardFailsIfLoggedOut() {
        initSetup()

        val signal = CountDownLatch(1)

        clearLoggedInPreferences()

        cards.unlockCard(cardId = 5) { result ->
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
    fun testReplaceCard() {
        initSetup()

        val signal = CountDownLatch(1)

        val cardId = 2L
        val requestPath = "cards/$cardId/replace"

        val body = readStringFromJson(app, R.raw.card_replace_refresh)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == requestPath) {
                    return MockResponse()
                        .setResponseCode(204)
                } else if (request?.trimmedPath == CardsAPI.URL_CARDS) {
                    return MockResponse()
                        .setResponseCode(200)
                        .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        val card = testCardResponseData(cardId = cardId, status = CardStatus.ACTIVE).toCard()

        database.cards().insert(card)

        val testObserver1 = cards.fetchCard(cardId = cardId).test()

        testObserver1.awaitValue()
        assertNotNull(testObserver1.value())
        assertEquals(cardId, testObserver1.value()?.cardId)
        assertEquals(CardStatus.ACTIVE, testObserver1.value()?.status)

        cards.replaceCard(cardId = cardId, reason = CardLockOrReplaceReason.FRAUD) { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            val testObserver = cards.fetchCards().test()

            testObserver.awaitValue()
            val models = testObserver.value()
            assertNotNull(models)
            assertEquals(2, models?.size)
            assertEquals(250L, models?.get(0)?.cardId)
            assertEquals(254L, models?.get(1)?.cardId)

            signal.countDown()
        }

        signal.await(3, TimeUnit.SECONDS)

        assertEquals(2, mockServer.requestCount)

        tearDown()
    }

    @Test
    fun testReplaceCardFailsIfLoggedOut() {
        initSetup()

        val signal = CountDownLatch(1)

        clearLoggedInPreferences()

        cards.replaceCard(cardId = 2, reason = CardLockOrReplaceReason.FRAUD) { result ->
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
    fun testGetCardPublicKey() {
        initSetup()

        val signal = CountDownLatch(1)

        val body = readStringFromJson(app, R.raw.card_public_key)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == CardsAPI.URL_CARD_PUBLIC_KEY) {
                    return MockResponse()
                        .setResponseCode(200)
                        .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        cards.getPublicKey { resource ->
            assertEquals(Resource.Status.SUCCESS, resource.status)
            assertNull(resource.error)

            assertEquals("1lj1-4ij1-2144", resource.data?.keyId)
            assertEquals("j32hnt3", resource.data?.publicKey)

            signal.countDown()
        }

        val request = mockServer.takeRequest()
        assertEquals(CardsAPI.URL_CARD_PUBLIC_KEY, request.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testGetCardPublicKeyFailsIfLoggedOut() {
        initSetup()

        val signal = CountDownLatch(1)

        clearLoggedInPreferences()

        cards.getPublicKey { resource ->
            assertEquals(Resource.Status.ERROR, resource.status)
            assertNotNull(resource.error)
            assertEquals(DataErrorType.AUTHENTICATION, (resource.error as DataError).type)
            assertEquals(DataErrorSubType.MISSING_ACCESS_TOKEN, (resource.error as DataError).subType)

            signal.countDown()
        }

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testCardsLinkToAccounts() {
        initSetup()

        database.accounts().insert(testAccountResponseData(accountId = 345, providerAccountId = 234).toAccount())
        database.cards().insert(testCardResponseData(cardId = 123, accountId = 345).toCard())

        val testObserver = cards.fetchCardWithRelation(cardId = 123).test()

        testObserver.awaitValue()
        val model = testObserver.value()
        assertNotNull(model)
        assertEquals(123L, model?.card?.cardId)
        assertEquals(345L, model?.account?.account?.accountId)

        tearDown()
    }
}
