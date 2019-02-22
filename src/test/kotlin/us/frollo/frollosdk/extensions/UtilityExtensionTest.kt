package us.frollo.frollosdk.extensions

import com.google.gson.Gson
import okhttp3.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import us.frollo.frollosdk.error.APIErrorType
import us.frollo.frollosdk.model.coredata.user.Attribution
import us.frollo.frollosdk.model.coredata.user.Gender


class UtilityExtensionTest {

    private lateinit var mOkHttpResponseBuilder: Response.Builder

    @Before
    fun setup() {
        val mHttpUrl = HttpUrl.Builder()
                .scheme("https")
                .host("test.frollosdk.com")
                .build()
        val mRequest = Request.Builder()
                .url(mHttpUrl)
                .build()

        // Prepare the builder with common stuff.
        mOkHttpResponseBuilder = Response.Builder()
                .request(mRequest)
                .protocol(Protocol.HTTP_1_1)
                .code(401)
                .body(ResponseBody.create(MediaType.parse("text"), "Unauthorized"))
                .message("Unauthorized Error")
    }

    @Test
    fun testIfNotNull() {
        var isBothNull = true
        val value1: Int? = 1
        var value2: Int? = null

        ifNotNull(value1, value2) { _, _ ->
            isBothNull = false
        }

        assertTrue(isBothNull)

        value2 = 2

        ifNotNull(value1, value2) { _, _ ->
            isBothNull = false
        }

        assertFalse(isBothNull)
    }

    @Test
    fun testGsonFromJson() {
        val json = "{\"network\":\"organic\",\"campaign\":\"frollo\"}"
        val attr: Attribution? =  Gson().fromJson(json)
        assertEquals("organic", attr?.network)
        assertEquals("frollo", attr?.campaign)
        assertNull(attr?.adGroup)
        assertNull(attr?.creative)
    }

    @Test
    fun testEnumToSerializedName() {
        assertEquals("male", Gender.MALE.serializedName())
        assertNull(APIErrorType.INVALID_ACCESS_TOKEN.serializedName())
    }

    @Test
    fun testResponseClonedBodyString() {
        val okHttpResponse = mOkHttpResponseBuilder.build()
        assertEquals("Unauthorized", okHttpResponse.clonedBodyString)
    }

    @Test
    fun testBooleanToInt() {
        assertEquals(1, true.toInt())
        assertEquals(0, false.toInt())
    }

    @Test
    fun testRegexValidate() {
        assertTrue("12345678".regexValidate("\\b\\d{8}\\b"))
    }

    @Test
    fun testCompareToFindMissingItems() {
        val set1 = setOf<Long>(1,2,3,7)
        val set2 = setOf<Long>(1,3,4,5,6)
        val missingElements = set1.compareToFindMissingItems(set2)
        assertEquals(2, missingElements.size)
        assertTrue(missingElements.containsAll(listOf<Long>(2, 7)))
    }
}