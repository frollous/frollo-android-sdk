package us.frollo.frollosdk.testutils

import retrofit2.Call
import retrofit2.http.*

internal interface TestAPI {
    companion object {
        const val URL_TEST = "test/data/"
    }

    @PUT(URL_TEST)
    fun testData(): Call<Void>
}