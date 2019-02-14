package us.frollo.frollosdk.mapping

import org.junit.Assert.*
import org.junit.Test
import us.frollo.frollosdk.model.testAccountResponseData
import us.frollo.frollosdk.model.testProviderAccountResponseData
import us.frollo.frollosdk.model.testProviderResponseData

class AggregationMappingTest {

    @Test
    fun testProviderResponseToProvider() {
        val response = testProviderResponseData(providerId = 12345)
        val model = response.toProvider()
        assertEquals(12345L, model.providerId)
    }

    @Test
    fun testProviderResponseToProviderAccount() {
        val response = testProviderAccountResponseData(providerAccountId = 12345)
        val model = response.toProviderAccount()
        assertEquals(12345L, model.providerAccountId)
    }

    @Test
    fun testAccountResponseToAccount() {
        val response = testAccountResponseData(accountId = 12345)
        val model = response.toAccount()
        assertEquals(12345L, model.accountId)
    }
}