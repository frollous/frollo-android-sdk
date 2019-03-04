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