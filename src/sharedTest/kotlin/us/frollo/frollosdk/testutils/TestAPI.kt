package us.frollo.frollosdk.testutils

import retrofit2.Call
import retrofit2.http.*
import us.frollo.frollosdk.network.NetworkHelper.Companion.API_VERSION_PATH

internal interface TestAPI {
    companion object {
        const val URL_TEST = "$API_VERSION_PATH/test/data/"
    }

    @PUT(URL_TEST)
    fun testData(): Call<Void>
}