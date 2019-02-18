package us.frollo.frollosdk.model.api.aggregation.accounts

import org.junit.Assert.*
import org.junit.Test
import us.frollo.frollosdk.model.testUpdateRequestData

class AccountUpdateRequestTest {

    @Test
    fun testValid() {
        var request = testUpdateRequestData(hidden = true, included = true)
        assertFalse(request.valid)

        request = testUpdateRequestData(hidden = false, included = true)
        assertTrue(request.valid)
    }
}