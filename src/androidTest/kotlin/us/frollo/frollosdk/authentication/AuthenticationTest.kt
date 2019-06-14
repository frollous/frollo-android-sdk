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

package us.frollo.frollosdk.authentication

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset

import us.frollo.frollosdk.BaseAndroidTest
import us.frollo.frollosdk.FrolloSDK
import us.frollo.frollosdk.base.Result
import us.frollo.frollosdk.core.OnFrolloSDKCompletionListener
import us.frollo.frollosdk.testutils.wait

class AuthenticationTest : BaseAndroidTest() {

    private lateinit var localAuthentication: TestCustomAuthentication

    override val isCustom: Boolean
        get() = true

    override fun initSetup() {
        super.initSetup()

        localAuthentication = authentication as TestCustomAuthentication
    }

    @Test
    fun testCustomAuthentication() {
        initSetup()

        assertEquals(localAuthentication, FrolloSDK.authentication)
        assertFalse(FrolloSDK.authentication.loggedIn)

        localAuthentication.login()

        assertTrue(FrolloSDK.authentication.loggedIn)
        assertEquals("AccessToken001", keystore.decrypt(preferences.encryptedAccessToken))
        assertNull(preferences.encryptedRefreshToken)
        assertNotNull(preferences.accessTokenExpiry)

        FrolloSDK.authentication.refreshTokens { result ->
            assertEquals(Result.Status.SUCCESS, result.status)
            assertNull(result.error)

            assertEquals("AccessToken002", keystore.decrypt(preferences.encryptedAccessToken))
            assertNull(preferences.encryptedRefreshToken)
            assertNotNull(preferences.accessTokenExpiry)
        }

        wait(3)

        tearDown()
    }
}

class TestCustomAuthentication: Authentication() {

    private var tokenIndex = 0
    private val validTokens = arrayOf("AccessToken001", "AccessToken002", "AccessToken003")

    override var loggedIn: Boolean = false

    override var authenticationCallback: AuthenticationCallback? = null

    fun login() {
        loggedIn = true

        authenticationCallback?.saveAccessTokens(
                accessToken = validTokens[tokenIndex],
                expiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 3600)
    }

    override fun refreshTokens(completion: OnFrolloSDKCompletionListener<Result>?) {
        if (tokenIndex < 2) {
            tokenIndex ++
        } else {
            tokenIndex = 0
        }

        authenticationCallback?.saveAccessTokens(
                accessToken = validTokens[tokenIndex],
                expiry = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + 3600)

        completion?.invoke(Result.success())
    }

    override fun logout() {
        reset()
    }

    override fun reset() {
        loggedIn = false

        tokenIndex = 0
    }
}