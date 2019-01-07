package us.frollo.frollosdk.extensions

import org.junit.Assert
import org.junit.Test
import us.frollo.frollosdk.mapping.toUser
import us.frollo.frollosdk.model.testUserResponseData

class ModelExtensionTest {

    @Test
    fun testUserResponseStripTokens() {
        val originalResponse = testUserResponseData()
        val strippedResponse = originalResponse.stripTokens()
        Assert.assertNull(strippedResponse.refreshToken)
        Assert.assertNull(strippedResponse.accessToken)
        Assert.assertNull(strippedResponse.accessTokenExp)
    }

    @Test
    fun testUserResponseFetchTokens() {
        val originalResponse = testUserResponseData()
        val tokenResponse = originalResponse.fetchTokens()
        Assert.assertEquals(originalResponse.refreshToken, tokenResponse.refreshToken)
        Assert.assertEquals(originalResponse.accessToken, tokenResponse.accessToken)
        Assert.assertEquals(originalResponse.accessTokenExp, tokenResponse.accessTokenExp)
    }

    @Test
    fun testUserUpdateRequest() {
        val user = testUserResponseData().toUser()
        val request = user.updateRequest()
        Assert.assertEquals(user.firstName, request.firstName)
    }
}