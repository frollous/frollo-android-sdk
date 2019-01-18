package us.frollo.frollosdk.data.local

import org.junit.Test

import org.junit.Assert.*
import us.frollo.frollosdk.model.coredata.messages.ContentType
import us.frollo.frollosdk.model.coredata.user.*

class ConvertersTest {

    @Test
    fun testStringToListOfString() {
        val string = "|welcome|welcome_event|event|"
        val list = Converters.instance.stringToListOfString(string)
        assertNotNull(list)
        assertTrue(list?.size == 3)
        assertEquals("welcome", list?.get(0))
        assertEquals("welcome_event", list?.get(1))
        assertEquals("event", list?.get(2))

        assertNull(Converters.instance.stringToListOfString(null))
    }

    @Test
    fun testStringFromListOfString() {
        val list = mutableListOf("welcome", "welcome_event", "event")
        val string = Converters.instance.stringFromListOfString(list)
        assertEquals("|welcome|welcome_event|event|", string)
    }

    @Test
    fun testStringToListOfFeatureFlag() {
        val json = "[{\"feature\":\"demo\",\"enabled\":true},{\"feature\":\"aggregation\",\"enabled\":false}]"
        val features = Converters.instance.stringToListOfFeatureFlag(json)
        assertNotNull(features)
        assertTrue(features?.size == 2)
        assertEquals("demo", features?.get(0)?.feature)
        assertEquals(true, features?.get(0)?.enabled)
        assertEquals("aggregation", features?.get(1)?.feature)
        assertEquals(false, features?.get(1)?.enabled)

        assertNull(Converters.instance.stringToListOfFeatureFlag(null))
    }

    @Test
    fun testStringFromListOfFeatureFlag() {
        val features = mutableListOf(FeatureFlag(feature = "demo", enabled = true), FeatureFlag(feature = "aggregation", enabled = false))
        val json = Converters.instance.stringFromListOfFeatureFlag(features)
        assertEquals("[{\"feature\":\"demo\",\"enabled\":true},{\"feature\":\"aggregation\",\"enabled\":false}]", json)
    }

    @Test
    fun testStringToUserStatus() {
        val status = Converters.instance.stringToUserStatus("BUDGET_READY")
        assertEquals(UserStatus.BUDGET_READY, status)

        assertNull(Converters.instance.stringToUserStatus(null))
    }

    @Test
    fun testStringFromUserStatus() {
        val str = Converters.instance.stringFromUserStatus(UserStatus.BUDGET_READY)
        assertEquals("BUDGET_READY", str)

        assertNull(Converters.instance.stringFromUserStatus(null))
    }

    @Test
    fun testStringToGender() {
        val status = Converters.instance.stringToGender("MALE")
        assertEquals(Gender.MALE, status)

        assertNull(Converters.instance.stringToGender(null))
    }

    @Test
    fun testStringFromGender() {
        val str = Converters.instance.stringFromGender(Gender.MALE)
        assertEquals("MALE", str)

        assertNull(Converters.instance.stringFromGender(null))
    }

    @Test
    fun testStringToHouseholdType() {
        val status = Converters.instance.stringToHouseholdType("COUPLE_WITH_KIDS")
        assertEquals(HouseholdType.COUPLE_WITH_KIDS, status)

        assertNull(Converters.instance.stringToHouseholdType(null))
    }

    @Test
    fun testStringFromHouseholdType() {
        val str = Converters.instance.stringFromHouseholdType(HouseholdType.COUPLE_WITH_KIDS)
        assertEquals("COUPLE_WITH_KIDS", str)

        assertNull(Converters.instance.stringFromHouseholdType(null))
    }

    @Test
    fun testStringToOccupation() {
        val status = Converters.instance.stringToOccupation("PROFESSIONALS")
        assertEquals(Occupation.PROFESSIONALS, status)

        assertNull(Converters.instance.stringToOccupation(null))
    }

    @Test
    fun testStringFromOccupation() {
        val str = Converters.instance.stringFromOccupation(Occupation.PROFESSIONALS)
        assertEquals("PROFESSIONALS", str)

        assertNull(Converters.instance.stringFromOccupation(null))
    }

    @Test
    fun testStringToIndustry() {
        val status = Converters.instance.stringToIndustry("EDUCATION_AND_TRAINING")
        assertEquals(Industry.EDUCATION_AND_TRAINING, status)

        assertNull(Converters.instance.stringToIndustry(null))
    }

    @Test
    fun testStringFromIndustry() {
        val str = Converters.instance.stringFromIndustry(Industry.EDUCATION_AND_TRAINING)
        assertEquals("EDUCATION_AND_TRAINING", str)

        assertNull(Converters.instance.stringFromIndustry(null))
    }

    @Test
    fun testStringToAttribution() {
        val json = "{\"network\":\"organic\",\"campaign\":\"frollo\"}"
        val attr = Converters.instance.stringToAttribution(json)
        assertNotNull(attr)
        assertEquals("organic", attr?.network)
        assertEquals("frollo", attr?.campaign)

        assertNull(Converters.instance.stringToAttribution(null))
    }

    @Test
    fun testStringFromAttribution() {
        val attr = Attribution(network = "organic", campaign = "frollo")
        val json = Converters.instance.stringFromAttribution(attr)
        assertEquals("{\"network\":\"organic\",\"campaign\":\"frollo\"}", json)
    }

    @Test
    fun testStringToContentType() {
        val status = Converters.instance.stringToContentType("IMAGE")
        assertEquals(ContentType.IMAGE, status)

        assertEquals(ContentType.TEXT, Converters.instance.stringToContentType(null))
    }

    @Test
    fun testStringFromContentType() {
        val str = Converters.instance.stringFromContentType(ContentType.IMAGE)
        assertEquals("IMAGE", str)

        assertEquals("TEXT", Converters.instance.stringFromContentType(null))
    }
}