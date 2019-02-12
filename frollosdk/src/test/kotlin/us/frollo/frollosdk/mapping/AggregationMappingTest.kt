package us.frollo.frollosdk.mapping

import org.junit.Assert.*
import org.junit.Test
import us.frollo.frollosdk.model.testProviderResponseData

class AggregationMappingTest {

    @Test
    fun testProviderResponseToProvider() {
        val providerResponse = testProviderResponseData(providerId = 12345)
        val provider = providerResponse.toProvider()
        assertEquals(12345L, provider.providerId)
    }
}