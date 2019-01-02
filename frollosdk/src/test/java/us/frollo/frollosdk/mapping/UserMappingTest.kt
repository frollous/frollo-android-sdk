package us.frollo.frollosdk.mapping

import org.junit.Assert.*
import org.junit.Test
import us.frollo.frollosdk.model.testDataUserResponse

class UserMappingTest {

    @Test
    fun testUserResponseToUser() {
        val userResponse = testDataUserResponse()
        val user = userResponse.toUser()
        assertEquals(userResponse.userId, user.userId)
    }
}