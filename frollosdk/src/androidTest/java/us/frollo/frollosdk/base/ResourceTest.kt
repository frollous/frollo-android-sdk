package us.frollo.frollosdk.base

import android.app.Application
import androidx.test.platform.app.InstrumentationRegistry
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import retrofit2.Response
import us.frollo.frollosdk.FrolloSDK
import us.frollo.frollosdk.R
import us.frollo.frollosdk.data.remote.ApiResponse
import us.frollo.frollosdk.error.FrolloSDKError

class ResourceTest {

    val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application

    @Before
    fun setup() {
        FrolloSDK.app = app
    }

    @Test
    fun testResource() {
        val resourceStr: Resource<String> = Resource.error(FrolloSDKError(errorMessage = "Unauthorized"), "12345")
        val resourceInt: Resource<Int> = resourceStr.map {
            assertNotNull(it)
            it?.toInt()
        }
        assertNotNull(resourceInt)
        assertNotNull(resourceInt.data)
        assertEquals(12345, resourceInt.data)
        assertNotNull(resourceInt.error)
        assertEquals("Unauthorized", resourceInt.error?.debugDescription)
        assertEquals("Unauthorized", resourceInt.error?.localizedDescription)
    }

    @Test
    fun testResourceSuccess() {
        val resource = Resource.success("12345")
        assertNotNull(resource)
        assertEquals(Resource.Status.SUCCESS, resource.status)
        assertNotNull(resource.data)
        assertEquals("12345", resource.data)
        assertNull(resource.error)
    }

    @Test
    fun testResourceLoading() {
        val resource = Resource.loading("12345")
        assertNotNull(resource)
        assertEquals(Resource.Status.LOADING, resource.status)
        assertNotNull(resource.data)
        assertEquals("12345", resource.data)
        assertNull(resource.error)
    }

    @Test
    fun testResourceError() {
        val resource = Resource.error(FrolloSDKError(errorMessage = "Unauthorized"), data = null)
        assertNotNull(resource)
        assertEquals(Resource.Status.ERROR, resource.status)
        assertNull(resource.data)
        assertNotNull(resource.error)
        assertEquals("Unauthorized", resource.error?.debugDescription)
        assertEquals("Unauthorized", resource.error?.localizedDescription)
    }

    @Test
    fun testSuccessFromApiResponse() {
        val apiResponse = ApiResponse<String>(Response.success("12345"))
        val resource: Resource<String> = Resource.fromApiResponse(apiResponse)
        assertNotNull(resource)
        assertEquals(Resource.Status.SUCCESS, resource.status)
        assertNotNull(resource.data)
        assertEquals("12345", resource.data)
        assertNull(resource.error)
    }

    @Test
    fun testErrorFromApiResponse() {
        val apiResponse = ApiResponse<String>(Response.error(401, ResponseBody.create(MediaType.parse("text"), "Unauthorized")))
        var resource: Resource<String> = Resource.fromApiResponse(apiResponse)
        assertNotNull(resource)
        assertEquals(Resource.Status.ERROR, resource.status)
        assertNull(resource.data)
        assertNotNull(resource.error)
        assertEquals("Unauthorized", resource.error?.debugDescription)
        assertEquals("Unauthorized", resource.error?.localizedDescription)

        apiResponse.errorMessage = null
        resource = Resource.fromApiResponse(apiResponse)
        assertNotNull(resource)
        assertEquals(Resource.Status.ERROR, resource.status)
        assertNull(resource.data)
        assertNotNull(resource.error)
        assertEquals(app.resources.getString(R.string.Error_Generic_UnknownError), resource.error?.debugDescription)
        assertEquals(app.resources.getString(R.string.Error_Generic_UnknownError), resource.error?.localizedDescription)
    }
}