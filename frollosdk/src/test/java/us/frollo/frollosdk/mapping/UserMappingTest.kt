package us.frollo.frollosdk.mapping

import org.junit.Assert.*
import org.junit.Test
import us.frollo.frollosdk.model.getTestUserResponse

class UserMappingTest {

    @Test
    fun testUserResponseToUser() {
        val userResponse = getTestUserResponse()
        val user = userResponse.toUser()
        assertEquals(userResponse.userId, user.userId)
    }
}