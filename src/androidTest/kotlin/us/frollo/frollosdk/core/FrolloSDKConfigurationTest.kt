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

package us.frollo.frollosdk.core

import org.junit.Assert.*
import org.junit.Test

class FrolloSDKConfigurationTest {

    @Test
    fun testValidForROPC() {
        var config = testSDKConfig()
        assertTrue(config.validForROPC())

        config = testSDKConfig(serverUrl = "")
        assertFalse(config.validForROPC())

        config = testSDKConfig(clientId = "")
        assertFalse(config.validForROPC())

        config = testSDKConfig(tokenUrl = "")
        assertFalse(config.validForROPC())

        config = testSDKConfig(authorizationUrl = "")
        assertTrue(config.validForROPC())

        config = testSDKConfig(redirectUrl = "")
        assertTrue(config.validForROPC())
    }

    @Test
    fun validForAuthorizationCodeFlow() {
        var config = testSDKConfig()
        assertTrue(config.validForAuthorizationCodeFlow())

        config = testSDKConfig(serverUrl = "")
        assertFalse(config.validForAuthorizationCodeFlow())

        config = testSDKConfig(clientId = "")
        assertFalse(config.validForAuthorizationCodeFlow())

        config = testSDKConfig(tokenUrl = "")
        assertFalse(config.validForAuthorizationCodeFlow())

        config = testSDKConfig(authorizationUrl = "")
        assertFalse(config.validForAuthorizationCodeFlow())

        config = testSDKConfig(redirectUrl = "")
        assertFalse(config.validForAuthorizationCodeFlow())
    }
}