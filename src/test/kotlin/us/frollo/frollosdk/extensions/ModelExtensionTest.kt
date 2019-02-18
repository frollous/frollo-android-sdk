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

    @Test
    fun testGenerateSQLQueryMessages() {
        val query = generateSQLQueryMessages(mutableListOf("survey", "event"), false)
        Assert.assertEquals("SELECT * FROM message WHERE ((message_types LIKE '%|survey|%') OR (message_types LIKE '%|event|%')) AND read = 0", query.sql)
    }

    @Test
    fun testSQLForTransactionStaleIds() {
        val query = sqlForTransactionStaleIds(fromDate = "2019-01-03", toDate = "2019-02-03", accountIds = longArrayOf(123,456), transactionIncluded = false)
        Assert.assertEquals("SELECT transaction_id FROM transaction_model WHERE ((transaction_date BETWEEN Date('2019-01-03') AND Date('2019-02-03'))  AND account_id IN (123,456)  AND included = 0 )", query.sql)
    }
}