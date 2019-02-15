package us.frollo.frollosdk.data.local

import org.junit.Test

import org.junit.Assert.*
import us.frollo.frollosdk.model.coredata.aggregation.accounts.*
import us.frollo.frollosdk.model.coredata.aggregation.provideraccounts.AccountRefreshAdditionalStatus
import us.frollo.frollosdk.model.coredata.aggregation.provideraccounts.AccountRefreshStatus
import us.frollo.frollosdk.model.coredata.aggregation.provideraccounts.AccountRefreshSubStatus
import us.frollo.frollosdk.model.coredata.aggregation.providers.*
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

    @Test
    fun testStringToProviderStatus() {
        val status = Converters.instance.stringToProviderStatus("SUPPORTED")
        assertEquals(ProviderStatus.SUPPORTED, status)

        assertNull(Converters.instance.stringToProviderStatus(null))
    }

    @Test
    fun testStringFromProviderStatus() {
        val str = Converters.instance.stringFromProviderStatus(ProviderStatus.SUPPORTED)
        assertEquals("SUPPORTED", str)

        assertNull(Converters.instance.stringFromProviderStatus(null))
    }

    @Test
    fun testStringToProviderAuthType() {
        val status = Converters.instance.stringToProviderAuthType("CREDENTIALS")
        assertEquals(ProviderAuthType.CREDENTIALS, status)

        assertEquals(ProviderAuthType.UNKNOWN, Converters.instance.stringToProviderAuthType(null))
    }

    @Test
    fun testStringFromProviderAuthType() {
        val str = Converters.instance.stringFromProviderAuthType(ProviderAuthType.CREDENTIALS)
        assertEquals("CREDENTIALS", str)

        assertEquals("UNKNOWN", Converters.instance.stringFromProviderAuthType(null))
    }

    @Test
    fun testStringToProviderMFAType() {
        val status = Converters.instance.stringToProviderMFAType("QUESTION")
        assertEquals(ProviderMFAType.QUESTION, status)

        assertEquals(ProviderMFAType.UNKNOWN, Converters.instance.stringToProviderMFAType(null))
    }

    @Test
    fun testStringFromProviderMFAType() {
        val str = Converters.instance.stringFromProviderMFAType(ProviderMFAType.QUESTION)
        assertEquals("QUESTION", str)

        assertEquals("UNKNOWN", Converters.instance.stringFromProviderMFAType(null))
    }

    @Test
    fun testStringToProviderLoginForm() {
        val json = "{\"id\":\"13039\",\"forgetPasswordURL\":\"https://ib.mebank.com.au/auth/ib/login.html\",\"formType\":\"login\",\"row\":[]}"
        val form = Converters.instance.stringToProviderLoginForm(json)
        assertNotNull(form)
        assertEquals("13039", form?.formId)
        assertEquals("https://ib.mebank.com.au/auth/ib/login.html", form?.forgetPasswordUrl)
        assertEquals(ProviderFormType.LOGIN, form?.formType)
        assertTrue(form?.rows?.isEmpty() == true)

        assertNull(Converters.instance.stringToProviderLoginForm(null))
    }

    @Test
    fun testStringFromProviderLoginForm() {
        val form = ProviderLoginForm(
                formId = "13039",
                forgetPasswordUrl = "https://ib.mebank.com.au/auth/ib/login.html",
                formType = ProviderFormType.LOGIN,
                rows = listOf(),
                help = null,
                mfaInfoText = null,
                mfaInfoTitle = null,
                mfaTimeout = null)
        val json = Converters.instance.stringFromProviderLoginForm(form)
        assertEquals("{\"id\":\"13039\",\"forgetPasswordURL\":\"https://ib.mebank.com.au/auth/ib/login.html\",\"formType\":\"login\",\"row\":[]}", json)
    }

    @Test
    fun testStringToProviderEncryptionType() {
        val status = Converters.instance.stringToProviderEncryptionType("ENCRYPT_VALUES")
        assertEquals(ProviderEncryptionType.ENCRYPT_VALUES, status)

        assertNull(Converters.instance.stringToProviderEncryptionType(null))
    }

    @Test
    fun testStringFromProviderEncryptionType() {
        val str = Converters.instance.stringFromProviderEncryptionType(ProviderEncryptionType.ENCRYPT_VALUES)
        assertEquals("ENCRYPT_VALUES", str)

        assertNull(Converters.instance.stringFromProviderEncryptionType(null))
    }

    @Test
    fun testStringToListOfProviderContainerName() {
        val string = "|bank|credit_card|bill|"
        val list = Converters.instance.stringToListOfProviderContainerName(string)
        assertNotNull(list)
        assertTrue(list?.size == 3)
        assertEquals(ProviderContainerName.BANK, list?.get(0))
        assertEquals(ProviderContainerName.CREDIT_CARD, list?.get(1))
        assertEquals(ProviderContainerName.BILL, list?.get(2))

        assertNull(Converters.instance.stringToListOfProviderContainerName(null))
    }

    @Test
    fun testStringFromListOfProviderContainerName() {
        val list = mutableListOf(ProviderContainerName.BANK, ProviderContainerName.CREDIT_CARD, ProviderContainerName.BILL)
        val string = Converters.instance.stringFromListOfProviderContainerName(list)
        assertEquals("|bank|credit_card|bill|", string)
    }

    @Test
    fun testStringToAccountRefreshStatus() {
        val status = Converters.instance.stringToAccountRefreshStatus("SUCCESS")
        assertEquals(AccountRefreshStatus.SUCCESS, status)

        assertEquals(AccountRefreshStatus.UPDATING, Converters.instance.stringToAccountRefreshStatus(null))
    }

    @Test
    fun testStringFromAccountRefreshStatus() {
        val str = Converters.instance.stringFromAccountRefreshStatus(AccountRefreshStatus.SUCCESS)
        assertEquals("SUCCESS", str)

        assertEquals("UPDATING", Converters.instance.stringFromAccountRefreshStatus(null))
    }

    @Test
    fun testStringToAccountRefreshSubStatus() {
        val status = Converters.instance.stringToAccountRefreshSubStatus("SUCCESS")
        assertEquals(AccountRefreshSubStatus.SUCCESS, status)

        assertNull(Converters.instance.stringToAccountRefreshSubStatus(null))
    }

    @Test
    fun testStringFromAccountRefreshSubStatus() {
        val str = Converters.instance.stringFromAccountRefreshSubStatus(AccountRefreshSubStatus.SUCCESS)
        assertEquals("SUCCESS", str)

        assertNull(Converters.instance.stringFromAccountRefreshSubStatus(null))
    }

    @Test
    fun testStringToAccountRefreshAdditionalStatus() {
        val status = Converters.instance.stringToAccountRefreshAdditionalStatus("ACCEPT_SPLASH")
        assertEquals(AccountRefreshAdditionalStatus.ACCEPT_SPLASH, status)

        assertNull(Converters.instance.stringToAccountRefreshAdditionalStatus(null))
    }

    @Test
    fun testStringFromAccountRefreshAdditionalStatus() {
        val str = Converters.instance.stringFromAccountRefreshAdditionalStatus(AccountRefreshAdditionalStatus.ACCEPT_SPLASH)
        assertEquals("ACCEPT_SPLASH", str)

        assertNull(Converters.instance.stringFromAccountRefreshAdditionalStatus(null))
    }

    @Test
    fun testStringToBigDecimal() {
        val value1 = (123.00001).toBigDecimal()
        val value2 = Converters.instance.stringToBigDecimal("123.00001")
        assertEquals(value1, value2)
    }

    @Test
    fun testStringFromBigDecimal() {
        val value = (123.00001).toBigDecimal()
        val valueStr = Converters.instance.stringFromBigDecimal(value)
        assertEquals("123.00001", valueStr)
    }

    @Test
    fun testStringToAccountStatus() {
        val status = Converters.instance.stringToAccountStatus("ACTIVE")
        assertEquals(AccountStatus.ACTIVE, status)

        assertNull(Converters.instance.stringToAccountStatus(null))
    }

    @Test
    fun testStringFromAccountStatus() {
        val str = Converters.instance.stringFromAccountStatus(AccountStatus.ACTIVE)
        assertEquals("ACTIVE", str)

        assertNull(Converters.instance.stringFromAccountStatus(null))
    }

    @Test
    fun testStringToAccountType() {
        val status = Converters.instance.stringToAccountType("BANK")
        assertEquals(AccountType.BANK, status)

        assertEquals(AccountType.UNKNOWN, Converters.instance.stringToAccountType(null))
    }

    @Test
    fun testStringFromAccountType() {
        val str = Converters.instance.stringFromAccountType(AccountType.BANK)
        assertEquals("BANK", str)

        assertEquals("UNKNOWN", Converters.instance.stringFromAccountType(null))
    }

    @Test
    fun testStringToAccountClassification() {
        val status = Converters.instance.stringToAccountClassification("ADD_ON_CARD")
        assertEquals(AccountClassification.ADD_ON_CARD, status)

        assertEquals(AccountClassification.OTHER, Converters.instance.stringToAccountClassification(null))
    }

    @Test
    fun testStringFromAccountClassification() {
        val str = Converters.instance.stringFromAccountClassification(AccountClassification.ADD_ON_CARD)
        assertEquals("ADD_ON_CARD", str)

        assertEquals("OTHER", Converters.instance.stringFromAccountClassification(null))
    }

    @Test
    fun testStringToAccountSubType() {
        val status = Converters.instance.stringToAccountSubType("AUTO_INSURANCE")
        assertEquals(AccountSubType.AUTO_INSURANCE, status)

        assertEquals(AccountSubType.OTHER, Converters.instance.stringToAccountSubType(null))
    }

    @Test
    fun testStringFromAccountSubType() {
        val str = Converters.instance.stringFromAccountSubType(AccountSubType.AUTO_INSURANCE)
        assertEquals("AUTO_INSURANCE", str)

        assertEquals("OTHER", Converters.instance.stringFromAccountSubType(null))
    }

    @Test
    fun testStringToAccountGroup() {
        val status = Converters.instance.stringToAccountGroup("INVESTMENT")
        assertEquals(AccountGroup.INVESTMENT, status)

        assertEquals(AccountGroup.OTHER, Converters.instance.stringToAccountGroup(null))
    }

    @Test
    fun testStringFromAccountGroup() {
        val str = Converters.instance.stringFromAccountGroup(AccountGroup.INVESTMENT)
        assertEquals("INVESTMENT", str)

        assertEquals("OTHER", Converters.instance.stringFromAccountGroup(null))
    }

    @Test
    fun testStringToListOfBalanceTier() {
        val json = "[{\"description\":\"Below average\",\"min\":0,\"max\":549},{\"description\":\"Above average\",\"min\":550,\"max\":700}]"
        val tiers = Converters.instance.stringToListOfBalanceTier(json)
        assertNotNull(tiers)
        assertTrue(tiers?.size == 2)
        assertEquals("Below average", tiers?.get(0)?.description)
        assertEquals(549, tiers?.get(0)?.max)
        assertEquals(0, tiers?.get(0)?.min)
        assertEquals("Above average", tiers?.get(1)?.description)
        assertEquals(700, tiers?.get(1)?.max)
        assertEquals(550, tiers?.get(1)?.min)

        assertNull(Converters.instance.stringToListOfBalanceTier(null))
    }

    @Test
    fun testStringFromListOfBalanceTier() {
        val tiers = mutableListOf(BalanceTier(description = "Below average", max = 549, min = 0), BalanceTier(description = "Above average", max = 700, min = 550))
        val json = Converters.instance.stringFromListOfBalanceTier(tiers)
        assertEquals("[{\"description\":\"Below average\",\"min\":0,\"max\":549},{\"description\":\"Above average\",\"min\":550,\"max\":700}]", json)
    }
}