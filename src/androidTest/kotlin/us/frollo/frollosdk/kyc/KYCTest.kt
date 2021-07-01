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

package us.frollo.frollosdk.kyc

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import us.frollo.frollosdk.BaseAndroidTest
import us.frollo.frollosdk.base.Resource
import us.frollo.frollosdk.error.DataError
import us.frollo.frollosdk.error.DataErrorSubType
import us.frollo.frollosdk.error.DataErrorType
import us.frollo.frollosdk.model.coredata.kyc.IdentityDocumentType
import us.frollo.frollosdk.model.coredata.kyc.KycStatus
import us.frollo.frollosdk.model.testKycResponseData
import us.frollo.frollosdk.network.api.KycAPI
import us.frollo.frollosdk.test.R
import us.frollo.frollosdk.testutils.readStringFromJson
import us.frollo.frollosdk.testutils.trimmedPath
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class KYCTest : BaseAndroidTest() {

    override fun initSetup() {
        super.initSetup()

        preferences.loggedIn = true
        preferences.encryptedAccessToken = keystore.encrypt("ExistingAccessToken")
        preferences.encryptedRefreshToken = keystore.encrypt("ExistingRefreshToken")
        preferences.accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 900
    }

    @Test
    fun testFetchKycNonExistent() {
        initSetup()

        val signal = CountDownLatch(1)

        val body = readStringFromJson(app, R.raw.kyc_response_non_existent)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == KycAPI.URL_KYC) {
                    return MockResponse()
                        .setResponseCode(200)
                        .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        kyc.fetchKyc { resource ->
            assertEquals(Resource.Status.SUCCESS, resource.status)
            assertNull(resource.error)

            val response = resource.data
            assertNotNull(response)

            assertEquals("drsheldon@frollo.us", response?.email)
            assertEquals(KycStatus.NON_EXISTENT, response?.status)
            assertNull(response?.dateOfBirth)
            assertNull(response?.gender)
            assertNull(response?.mobileNumber)
            assertNull(response?.name)
            assertNull(response?.identityDocuments)

            signal.countDown()
        }

        val request = mockServer.takeRequest()
        assertEquals(KycAPI.URL_KYC, request.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testFetchKyc() {
        initSetup()

        val signal = CountDownLatch(1)

        val body = readStringFromJson(app, R.raw.kyc_response)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == KycAPI.URL_KYC) {
                    return MockResponse()
                        .setResponseCode(200)
                        .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        kyc.fetchKyc { resource ->
            assertEquals(Resource.Status.SUCCESS, resource.status)
            assertNull(resource.error)

            val response = resource.data
            assertNotNull(response)

            assertEquals("1991", response?.dateOfBirth?.yearOfBirth)
            assertEquals("1991-01-01", response?.dateOfBirth?.dateOfBirth)

            assertEquals("M", response?.gender)
            assertEquals("drsheldon@frollo.us", response?.email)
            assertEquals("0421354444", response?.mobileNumber)

            assertEquals("Sheldon", response?.name?.givenName)
            assertEquals("k", response?.name?.middleName)
            assertEquals("Shelly", response?.name?.displayName)
            assertEquals("Cooper", response?.name?.familyName)
            assertEquals("Dr", response?.name?.honourific)

            assertEquals("123456", response?.identityDocuments?.get(0)?.idNumber)
            assertEquals(IdentityDocumentType.BIRTH_CERTIFICATE, response?.identityDocuments?.get(0)?.idType)
            assertEquals("certificate", response?.identityDocuments?.get(0)?.idSubType)
            assertEquals("Sydney", response?.identityDocuments?.get(0)?.region)
            assertEquals("AU", response?.identityDocuments?.get(0)?.country)

            assertEquals("234567", response?.identityDocuments?.get(1)?.idNumber)
            assertEquals(IdentityDocumentType.DRIVERS_LICENCE, response?.identityDocuments?.get(1)?.idType)
            assertEquals("NSW license", response?.identityDocuments?.get(1)?.idSubType)
            assertEquals("NSW", response?.identityDocuments?.get(1)?.region)
            assertEquals("AU", response?.identityDocuments?.get(1)?.country)
            assertEquals("2022-12-12", response?.identityDocuments?.get(1)?.idExpiry)

            assertEquals("345678", response?.identityDocuments?.get(2)?.idNumber)
            assertEquals(IdentityDocumentType.DEVICE, response?.identityDocuments?.get(2)?.idType)
            assertEquals("no idea type", response?.identityDocuments?.get(2)?.idSubType)
            assertEquals("NSW", response?.identityDocuments?.get(2)?.region)
            assertEquals("AU", response?.identityDocuments?.get(2)?.country)
            assertEquals("2022-12-12", response?.identityDocuments?.get(2)?.idExpiry)

            assertEquals(KycStatus.VERIFIED, response?.status)

            signal.countDown()
        }

        val request = mockServer.takeRequest()
        assertEquals(KycAPI.URL_KYC, request.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testFetchKycLoggedOut() {
        initSetup()

        val signal = CountDownLatch(1)

        clearLoggedInPreferences()

        kyc.fetchKyc { resource ->
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
    fun testCreateKyc() {
        initSetup()

        val signal = CountDownLatch(1)

        val body = readStringFromJson(app, R.raw.kyc_response)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == KycAPI.URL_KYC_CREATE_VERIFY) {
                    return MockResponse()
                        .setResponseCode(200)
                        .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        kyc.submitKyc(testKycResponseData()) { resource ->
            assertEquals(Resource.Status.SUCCESS, resource.status)
            assertNull(resource.error)

            val response = resource.data
            assertNotNull(response)

            assertEquals("1991", response?.dateOfBirth?.yearOfBirth)
            assertEquals("1991-01-01", response?.dateOfBirth?.dateOfBirth)

            assertEquals("M", response?.gender)
            assertEquals("drsheldon@frollo.us", response?.email)
            assertEquals("0421354444", response?.mobileNumber)

            assertEquals("Sheldon", response?.name?.givenName)
            assertEquals("k", response?.name?.middleName)
            assertEquals("Shelly", response?.name?.displayName)
            assertEquals("Cooper", response?.name?.familyName)
            assertEquals("Dr", response?.name?.honourific)

            assertEquals("123456", response?.identityDocuments?.get(0)?.idNumber)
            assertEquals(IdentityDocumentType.BIRTH_CERTIFICATE, response?.identityDocuments?.get(0)?.idType)
            assertEquals("certificate", response?.identityDocuments?.get(0)?.idSubType)
            assertEquals("Sydney", response?.identityDocuments?.get(0)?.region)
            assertEquals("AU", response?.identityDocuments?.get(0)?.country)

            assertEquals("234567", response?.identityDocuments?.get(1)?.idNumber)
            assertEquals(IdentityDocumentType.DRIVERS_LICENCE, response?.identityDocuments?.get(1)?.idType)
            assertEquals("NSW license", response?.identityDocuments?.get(1)?.idSubType)
            assertEquals("NSW", response?.identityDocuments?.get(1)?.region)
            assertEquals("AU", response?.identityDocuments?.get(1)?.country)
            assertEquals("2022-12-12", response?.identityDocuments?.get(1)?.idExpiry)

            assertEquals("345678", response?.identityDocuments?.get(2)?.idNumber)
            assertEquals(IdentityDocumentType.DEVICE, response?.identityDocuments?.get(2)?.idType)
            assertEquals("no idea type", response?.identityDocuments?.get(2)?.idSubType)
            assertEquals("NSW", response?.identityDocuments?.get(2)?.region)
            assertEquals("AU", response?.identityDocuments?.get(2)?.country)
            assertEquals("2022-12-12", response?.identityDocuments?.get(2)?.idExpiry)

            assertEquals(KycStatus.VERIFIED, response?.status)

            signal.countDown()
        }

        val request = mockServer.takeRequest()
        assertEquals(KycAPI.URL_KYC_CREATE_VERIFY, request.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testUpdateKyc() {
        initSetup()

        val signal = CountDownLatch(1)

        val body = readStringFromJson(app, R.raw.kyc_response_medicare)
        mockServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                if (request?.trimmedPath == KycAPI.URL_KYC_CREATE_VERIFY) {
                    return MockResponse()
                        .setResponseCode(200)
                        .setBody(body)
                }
                return MockResponse().setResponseCode(404)
            }
        })

        kyc.submitKyc(testKycResponseData()) { resource ->
            assertEquals(Resource.Status.SUCCESS, resource.status)
            assertNull(resource.error)

            val response = resource.data
            assertNotNull(response)

            assertEquals("1950-01-01", response?.dateOfBirth?.dateOfBirth)

            assertEquals("M", response?.gender)
            assertEquals("gaetan+new@frollo.us", response?.email)
            assertEquals("+61411458987", response?.mobileNumber)

            assertEquals("JAMES", response?.name?.givenName)
            assertEquals("A", response?.name?.middleName)
            assertEquals("JAMES A TESTONE", response?.name?.displayName)
            assertEquals("TESTONE", response?.name?.familyName)

            assertEquals("66c7bbea-5b12-fd8b-d6db-1e76592a1520", response?.identityDocuments?.get(0)?.documentId)
            assertEquals("AUS", response?.identityDocuments?.get(0)?.country)
            assertEquals("283229690", response?.identityDocuments?.get(0)?.idNumber)
            assertEquals(IdentityDocumentType.DRIVERS_LICENCE, response?.identityDocuments?.get(0)?.idType)
            assertEquals("VIC", response?.identityDocuments?.get(0)?.region)
            assertEquals("0001-01-01", response?.identityDocuments?.get(0)?.idExpiry)
            assertEquals("0001-01-01", response?.identityDocuments?.get(1)?.idIssued)

            assertEquals("b36bf68f-66ea-a6d2-9d54-54a6d592f322", response?.identityDocuments?.get(1)?.documentId)
            assertEquals("AUS", response?.identityDocuments?.get(1)?.country)
            assertEquals("6603984391", response?.identityDocuments?.get(1)?.idNumber)
            assertEquals(IdentityDocumentType.NATIONAL_HEALTH_ID, response?.identityDocuments?.get(1)?.idType)
            assertEquals("G", response?.identityDocuments?.get(1)?.idSubType)
            assertEquals("0001-01-01", response?.identityDocuments?.get(1)?.idExpiry)
            assertEquals("0001-01-01", response?.identityDocuments?.get(1)?.idIssued)

            assertEquals(1, response?.identityDocuments?.get(1)?.extraData?.size)
            assertEquals("reference", response?.identityDocuments?.get(1)?.extraData?.get(0)?.kvpKey)
            assertEquals("general.integer", response?.identityDocuments?.get(1)?.extraData?.get(0)?.kvpType)
            assertEquals("1", response?.identityDocuments?.get(1)?.extraData?.get(0)?.kvpValue)

            assertEquals(KycStatus.UNVERIFIED, response?.status)

            signal.countDown()
        }

        val request = mockServer.takeRequest()
        assertEquals(KycAPI.URL_KYC_CREATE_VERIFY, request.trimmedPath)

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }

    @Test
    fun testSubmitKycLoggedOut() {
        initSetup()

        val signal = CountDownLatch(1)

        clearLoggedInPreferences()

        kyc.submitKyc(testKycResponseData()) { resource ->
            assertEquals(Resource.Status.ERROR, resource.status)
            assertNotNull(resource.error)
            assertEquals(DataErrorType.AUTHENTICATION, (resource.error as DataError).type)
            assertEquals(DataErrorSubType.MISSING_ACCESS_TOKEN, (resource.error as DataError).subType)

            signal.countDown()
        }

        signal.await(3, TimeUnit.SECONDS)

        tearDown()
    }
}
