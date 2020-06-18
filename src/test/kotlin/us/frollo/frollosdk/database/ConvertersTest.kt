/*
 * Copyright 2019 Frollo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package us.frollo.frollosdk.database

import com.google.gson.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import us.frollo.frollosdk.model.coredata.aggregation.accounts.AccountClassification
import us.frollo.frollosdk.model.coredata.aggregation.accounts.AccountGroup
import us.frollo.frollosdk.model.coredata.aggregation.accounts.AccountStatus
import us.frollo.frollosdk.model.coredata.aggregation.accounts.AccountSubType
import us.frollo.frollosdk.model.coredata.aggregation.accounts.AccountType
import us.frollo.frollosdk.model.coredata.aggregation.accounts.BalanceTier
import us.frollo.frollosdk.model.coredata.aggregation.merchants.MerchantLocation
import us.frollo.frollosdk.model.coredata.aggregation.merchants.MerchantType
import us.frollo.frollosdk.model.coredata.aggregation.provideraccounts.AccountRefreshAdditionalStatus
import us.frollo.frollosdk.model.coredata.aggregation.provideraccounts.AccountRefreshStatus
import us.frollo.frollosdk.model.coredata.aggregation.provideraccounts.AccountRefreshSubStatus
import us.frollo.frollosdk.model.coredata.aggregation.providers.AggregatorType
import us.frollo.frollosdk.model.coredata.aggregation.providers.ProviderAuthType
import us.frollo.frollosdk.model.coredata.aggregation.providers.ProviderContainerName
import us.frollo.frollosdk.model.coredata.aggregation.providers.ProviderEncryptionType
import us.frollo.frollosdk.model.coredata.aggregation.providers.ProviderFormType
import us.frollo.frollosdk.model.coredata.aggregation.providers.ProviderLoginForm
import us.frollo.frollosdk.model.coredata.aggregation.providers.ProviderMFAType
import us.frollo.frollosdk.model.coredata.aggregation.providers.ProviderPermission
import us.frollo.frollosdk.model.coredata.aggregation.providers.ProviderStatus
import us.frollo.frollosdk.model.coredata.aggregation.transactioncategories.TransactionCategoryType
import us.frollo.frollosdk.model.coredata.aggregation.transactions.TransactionBaseType
import us.frollo.frollosdk.model.coredata.aggregation.transactions.TransactionStatus
import us.frollo.frollosdk.model.coredata.bills.BillFrequency
import us.frollo.frollosdk.model.coredata.bills.BillPaymentStatus
import us.frollo.frollosdk.model.coredata.bills.BillStatus
import us.frollo.frollosdk.model.coredata.bills.BillType
import us.frollo.frollosdk.model.coredata.budgets.BudgetFrequency
import us.frollo.frollosdk.model.coredata.budgets.BudgetStatus
import us.frollo.frollosdk.model.coredata.budgets.BudgetTrackingStatus
import us.frollo.frollosdk.model.coredata.budgets.BudgetType
import us.frollo.frollosdk.model.coredata.goals.GoalFrequency
import us.frollo.frollosdk.model.coredata.goals.GoalStatus
import us.frollo.frollosdk.model.coredata.goals.GoalTarget
import us.frollo.frollosdk.model.coredata.goals.GoalTrackingStatus
import us.frollo.frollosdk.model.coredata.goals.GoalTrackingType
import us.frollo.frollosdk.model.coredata.messages.ContentType
import us.frollo.frollosdk.model.coredata.messages.OpenMode
import us.frollo.frollosdk.model.coredata.reports.ReportGrouping
import us.frollo.frollosdk.model.coredata.reports.ReportPeriod
import us.frollo.frollosdk.model.coredata.shared.BudgetCategory
import us.frollo.frollosdk.model.coredata.user.Attribution
import us.frollo.frollosdk.model.coredata.user.FeatureFlag
import us.frollo.frollosdk.model.coredata.user.Gender
import us.frollo.frollosdk.model.coredata.user.HouseholdType
import us.frollo.frollosdk.model.coredata.user.Industry
import us.frollo.frollosdk.model.coredata.user.Occupation
import us.frollo.frollosdk.model.coredata.user.UserStatus
import java.math.BigDecimal

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
    fun testStringToOpenMode() {
        val status = Converters.instance.stringToOpenMode("INTERNAL")
        assertEquals(OpenMode.INTERNAL, status)
        assertEquals(OpenMode.INTERNAL, Converters.instance.stringToOpenMode(null))
    }

    @Test
    fun testStringFromOpenMode() {
        val str = Converters.instance.stringFromOpenMode(OpenMode.INTERNAL)
        assertEquals("INTERNAL", str)
        assertEquals("INTERNAL", Converters.instance.stringFromOpenMode(null))
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
            mfaTimeout = null
        )
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
    fun testStringToAggregatorType() {
        val status = Converters.instance.stringToAggregatorType("CDR")
        assertEquals(AggregatorType.CDR, status)

        assertEquals(AggregatorType.YODLEE, Converters.instance.stringToAggregatorType(null))
    }

    @Test
    fun testStringFromAggregatorType() {
        val str = Converters.instance.stringFromAggregatorType(AggregatorType.CDR)
        assertEquals("CDR", str)

        assertEquals("YODLEE", Converters.instance.stringFromAggregatorType(AggregatorType.YODLEE))
        assertEquals("YODLEE", Converters.instance.stringFromAggregatorType(null))
    }

    @Test
    fun testStringToListOfProviderPermission() {
        val string = "|customer_details|transaction_details|account_details|"
        val list = Converters.instance.stringToListOfProviderPermission(string)
        assertNotNull(list)
        assertTrue(list?.size == 3)
        assertEquals(ProviderPermission.CUSTOMER_DETAILS, list?.get(0))
        assertEquals(ProviderPermission.TRANSACTION_DETAILS, list?.get(1))
        assertEquals(ProviderPermission.ACCOUNT_DETAILS, list?.get(2))

        assertNull(Converters.instance.stringToListOfProviderPermission(null))
    }

    @Test
    fun testStringFromListOfProviderPermission() {
        val list = mutableListOf(ProviderPermission.CUSTOMER_DETAILS, ProviderPermission.TRANSACTION_DETAILS, ProviderPermission.ACCOUNT_DETAILS)
        val string = Converters.instance.stringFromListOfProviderPermission(list)
        assertEquals("|customer_details|transaction_details|account_details|", string)
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

    @Test
    fun testStringToTransactionBaseType() {
        val status = Converters.instance.stringToTransactionBaseType("CREDIT")
        assertEquals(TransactionBaseType.CREDIT, status)

        assertEquals(TransactionBaseType.UNKNOWN, Converters.instance.stringToTransactionBaseType(null))
    }

    @Test
    fun testStringFromTransactionBaseType() {
        val str = Converters.instance.stringFromTransactionBaseType(TransactionBaseType.CREDIT)
        assertEquals("CREDIT", str)

        assertEquals("UNKNOWN", Converters.instance.stringFromTransactionBaseType(null))
    }

    @Test
    fun testStringToTransactionStatus() {
        val status = Converters.instance.stringToTransactionStatus("POSTED")
        assertEquals(TransactionStatus.POSTED, status)

        assertNull(Converters.instance.stringToTransactionStatus(null))
    }

    @Test
    fun testStringFromTransactionStatus() {
        val str = Converters.instance.stringFromTransactionStatus(TransactionStatus.POSTED)
        assertEquals("POSTED", str)

        assertNull(Converters.instance.stringFromTransactionStatus(null))
    }

    @Test
    fun testStringToMerchantLocation() {
        val json = "{\"formatted_address\":\"41 McLaren St, North Sydney, NSW 2120 Australia\",\"line_1\":\"41 McLaren St\",\"suburb\":\"North Sydney\",\"state\":\"NSW\",\"postcode\":\"2120\",\"country\":\"Australia\",\"latitude\":-33.83517030000001,\"longitude\":151.2086038}"
        val location = Converters.instance.stringToMerchantLocation(json)
        assertNotNull(location)
        assertEquals("41 McLaren St, North Sydney, NSW 2120 Australia", location?.formattedAddress)
        assertEquals("41 McLaren St", location?.line1)
        assertNull(location?.line2)
        assertNull(location?.line3)
        assertEquals("North Sydney", location?.suburb)
        assertEquals("NSW", location?.state)
        assertEquals("2120", location?.postcode)
        assertEquals("Australia", location?.country)
        assertEquals(BigDecimal("-33.83517030000001"), location?.latitude)
        assertEquals(BigDecimal("151.2086038"), location?.longitude)

        assertNull(Converters.instance.stringToMerchantLocation(null))
    }

    @Test
    fun testStringFromMerchantLocation() {
        val location = MerchantLocation(
            formattedAddress = "41 McLaren St, North Sydney, NSW 2120 Australia",
            line1 = "41 McLaren St",
            line2 = null,
            line3 = null,
            suburb = "North Sydney",
            state = "NSW",
            postcode = "2120",
            country = "Australia",
            latitude = BigDecimal("-33.83517030000001"),
            longitude = BigDecimal("151.2086038")
        )
        val json = Converters.instance.stringFromMerchantLocation(location)
        assertEquals("{\"formatted_address\":\"41 McLaren St, North Sydney, NSW 2120 Australia\",\"line_1\":\"41 McLaren St\",\"suburb\":\"North Sydney\",\"state\":\"NSW\",\"postcode\":\"2120\",\"country\":\"Australia\",\"latitude\":-33.83517030000001,\"longitude\":151.2086038}", json)
    }

    @Test
    fun testStringToBudgetCategory() {
        val status = Converters.instance.stringToBudgetCategory("INCOME")
        assertEquals(BudgetCategory.INCOME, status)

        assertNull(Converters.instance.stringToBudgetCategory(null))
    }

    @Test
    fun testStringFromBudgetCategory() {
        val str = Converters.instance.stringFromBudgetCategory(BudgetCategory.INCOME)
        assertEquals("INCOME", str)

        assertNull(Converters.instance.stringFromBudgetCategory(null))
    }

    @Test
    fun testStringToTransactionCategoryType() {
        val status = Converters.instance.stringToTransactionCategoryType("INCOME")
        assertEquals(TransactionCategoryType.INCOME, status)

        assertEquals(TransactionCategoryType.UNCATEGORIZED, Converters.instance.stringToTransactionCategoryType(null))
    }

    @Test
    fun testStringFromTransactionCategoryType() {
        val str = Converters.instance.stringFromTransactionCategoryType(TransactionCategoryType.INCOME)
        assertEquals("INCOME", str)

        assertEquals("UNCATEGORIZED", Converters.instance.stringFromTransactionCategoryType(null))
    }

    @Test
    fun testStringToMerchantType() {
        val status = Converters.instance.stringToMerchantType("RETAILER")
        assertEquals(MerchantType.RETAILER, status)

        assertEquals(MerchantType.UNKNOWN, Converters.instance.stringToMerchantType(null))
    }

    @Test
    fun testStringFromMerchantType() {
        val str = Converters.instance.stringFromMerchantType(MerchantType.RETAILER)
        assertEquals("RETAILER", str)

        assertEquals("UNKNOWN", Converters.instance.stringFromMerchantType(null))
    }

    @Test
    fun testStringToReportGrouping() {
        val status = Converters.instance.stringToReportGrouping("BUDGET_CATEGORY")
        assertEquals(ReportGrouping.BUDGET_CATEGORY, status)

        assertNull(Converters.instance.stringToReportGrouping(null))
    }

    @Test
    fun testStringFromReportGrouping() {
        val str = Converters.instance.stringFromReportGrouping(ReportGrouping.BUDGET_CATEGORY)
        assertEquals("BUDGET_CATEGORY", str)

        assertNull(Converters.instance.stringFromReportGrouping(null))
    }

    @Test
    fun testStringToReportPeriod() {
        val status = Converters.instance.stringToReportPeriod("MONTH")
        assertEquals(ReportPeriod.MONTH, status)

        assertNull(Converters.instance.stringToReportPeriod(null))
    }

    @Test
    fun testStringFromReportPeriod() {
        val str = Converters.instance.stringFromReportPeriod(ReportPeriod.MONTH)
        assertEquals("MONTH", str)

        assertNull(Converters.instance.stringFromReportPeriod(null))
    }

    @Test
    fun testStringToListOfLong() {
        val string = "|1024790|1024791|1024792|"
        val list = Converters.instance.stringToListOfLong(string)
        assertNotNull(list)
        assertTrue(list?.size == 3)
        assertEquals(1024790L, list?.get(0))
        assertEquals(1024791L, list?.get(1))
        assertEquals(1024792L, list?.get(2))

        assertNull(Converters.instance.stringToListOfString(null))
    }

    @Test
    fun testStringFromListOfLong() {
        val list = mutableListOf<Long>(1024790, 1024791, 1024792)
        val string = Converters.instance.stringFromListOfLong(list)
        assertEquals("|1024790|1024791|1024792|", string)
    }

    @Test
    fun testStringToBillType() {
        val status = Converters.instance.stringToBillType("MANUAL")
        assertEquals(BillType.MANUAL, status)

        assertEquals(BillType.BILL, Converters.instance.stringToBillType(null))
    }

    @Test
    fun testStringFromBillType() {
        val str = Converters.instance.stringFromBillType(BillType.MANUAL)
        assertEquals("MANUAL", str)

        assertEquals("BILL", Converters.instance.stringFromBillType(null))
    }

    @Test
    fun testStringToBillStatus() {
        val status = Converters.instance.stringToBillStatus("CONFIRMED")
        assertEquals(BillStatus.CONFIRMED, status)

        assertEquals(BillStatus.ESTIMATED, Converters.instance.stringToBillStatus(null))
    }

    @Test
    fun testStringFromBillStatus() {
        val str = Converters.instance.stringFromBillStatus(BillStatus.CONFIRMED)
        assertEquals("CONFIRMED", str)

        assertEquals("ESTIMATED", Converters.instance.stringFromBillStatus(null))
    }

    @Test
    fun testStringToBillFrequency() {
        val status = Converters.instance.stringToBillFrequency("ANNUALLY")
        assertEquals(BillFrequency.ANNUALLY, status)

        assertEquals(BillFrequency.UNKNOWN, Converters.instance.stringToBillFrequency(null))
    }

    @Test
    fun testStringFromBillFrequency() {
        val str = Converters.instance.stringFromBillFrequency(BillFrequency.ANNUALLY)
        assertEquals("ANNUALLY", str)

        assertEquals("UNKNOWN", Converters.instance.stringFromBillFrequency(null))
    }

    @Test
    fun testStringToBillPaymentStatus() {
        val status = Converters.instance.stringToBillPaymentStatus("FUTURE")
        assertEquals(BillPaymentStatus.FUTURE, status)

        assertEquals(BillPaymentStatus.DUE, Converters.instance.stringToBillPaymentStatus(null))
    }

    @Test
    fun testStringFromBillPaymentStatus() {
        val str = Converters.instance.stringFromBillPaymentStatus(BillPaymentStatus.FUTURE)
        assertEquals("FUTURE", str)

        assertEquals("DUE", Converters.instance.stringFromBillPaymentStatus(null))
    }

    @Test
    fun testStringToGoalTrackingType() {
        val status = Converters.instance.stringToGoalTrackingType("CREDIT")
        assertEquals(GoalTrackingType.CREDIT, status)

        assertEquals(GoalTrackingType.DEBIT_CREDIT, Converters.instance.stringToGoalTrackingType(null))
    }

    @Test
    fun testStringFromGoalTrackingType() {
        val str = Converters.instance.stringFromGoalTrackingType(GoalTrackingType.CREDIT)
        assertEquals("CREDIT", str)

        assertEquals("DEBIT_CREDIT", Converters.instance.stringFromGoalTrackingType(null))
    }

    @Test
    fun testStringToGoalTrackingStatus() {
        val status = Converters.instance.stringToGoalTrackingStatus("AHEAD")
        assertEquals(GoalTrackingStatus.AHEAD, status)

        assertEquals(GoalTrackingStatus.ON_TRACK, Converters.instance.stringToGoalTrackingStatus(null))
    }

    @Test
    fun testStringFromGoalTrackingStatus() {
        val str = Converters.instance.stringFromGoalTrackingStatus(GoalTrackingStatus.AHEAD)
        assertEquals("AHEAD", str)

        assertEquals("ON_TRACK", Converters.instance.stringFromGoalTrackingStatus(null))
    }

    @Test
    fun testStringToGoalTarget() {
        val status = Converters.instance.stringToGoalTarget("AMOUNT")
        assertEquals(GoalTarget.AMOUNT, status)

        assertEquals(GoalTarget.OPEN_ENDED, Converters.instance.stringToGoalTarget(null))
    }

    @Test
    fun testStringFromGoalTarget() {
        val str = Converters.instance.stringFromGoalTarget(GoalTarget.AMOUNT)
        assertEquals("AMOUNT", str)

        assertEquals("OPEN_ENDED", Converters.instance.stringFromGoalTarget(null))
    }

    @Test
    fun testStringToGoalStatus() {
        val status = Converters.instance.stringToGoalStatus("ACTIVE")
        assertEquals(GoalStatus.ACTIVE, status)

        assertEquals(GoalStatus.UNSTARTED, Converters.instance.stringToGoalStatus(null))
    }

    @Test
    fun testStringFromGoalStatus() {
        val str = Converters.instance.stringFromGoalStatus(GoalStatus.ACTIVE)
        assertEquals("ACTIVE", str)

        assertEquals("UNSTARTED", Converters.instance.stringFromGoalStatus(null))
    }

    @Test
    fun testStringToGoalFrequency() {
        val status = Converters.instance.stringToGoalFrequency("ANNUALLY")
        assertEquals(GoalFrequency.ANNUALLY, status)

        assertEquals(GoalFrequency.SINGULAR, Converters.instance.stringToGoalFrequency(null))
    }

    @Test
    fun testStringFromGoalFrequency() {
        val str = Converters.instance.stringFromGoalFrequency(GoalFrequency.ANNUALLY)
        assertEquals("ANNUALLY", str)

        assertEquals("SINGULAR", Converters.instance.stringFromGoalFrequency(null))
    }

    @Test
    fun testStringToBudgetTrackingStatus() {
        val status = Converters.instance.stringToBudgetTrackingStatus("ON_TRACK")
        assertEquals(BudgetTrackingStatus.ON_TRACK, status)

        assertEquals(BudgetTrackingStatus.ON_TRACK, Converters.instance.stringToBudgetTrackingStatus(null))
    }

    @Test
    fun testStringFromBudgetTrackingStatus() {
        val str = Converters.instance.stringFromBudgetTrackingStatus(BudgetTrackingStatus.ON_TRACK)
        assertEquals("ON_TRACK", str)
        assertEquals("ON_TRACK", Converters.instance.stringFromBudgetTrackingStatus(null))
    }

    @Test
    fun testStringToBudgetStatus() {
        val status = Converters.instance.stringToBudgetStatus("UNSTARTED")
        assertEquals(BudgetStatus.UNSTARTED, status)
        assertEquals(BudgetStatus.UNSTARTED, Converters.instance.stringToBudgetStatus(null))
    }

    @Test
    fun testStringFromBudgetStatus() {
        val str = Converters.instance.stringFromBudgetStatus(BudgetStatus.UNSTARTED)
        assertEquals("UNSTARTED", str)
        assertEquals("UNSTARTED", Converters.instance.stringFromBudgetStatus(null))
    }

    @Test
    fun testStringToBudgetFrequency() {
        val status = Converters.instance.stringToBudgetFrequency("MONTHLY")
        assertEquals(BudgetFrequency.MONTHLY, status)
        assertEquals(BudgetFrequency.MONTHLY, Converters.instance.stringToBudgetFrequency(null))
    }

    @Test
    fun testStringFromBudgetFrequency() {
        val str = Converters.instance.stringFromBudgetFrequency(BudgetFrequency.MONTHLY)
        assertEquals("MONTHLY", str)

        assertEquals("MONTHLY", Converters.instance.stringFromBudgetFrequency(null))
    }

    @Test
    fun testStringToBudgetType() {
        val status = Converters.instance.stringToBudgetType("BUDGET_CATEGORY")
        assertEquals(BudgetType.BUDGET_CATEGORY, status)

        assertEquals(BudgetType.BUDGET_CATEGORY, Converters.instance.stringToBudgetType(null))
    }

    @Test
    fun testStringFromBudgetType() {
        val str = Converters.instance.stringFromBudgetType(BudgetType.BUDGET_CATEGORY)
        assertEquals("BUDGET_CATEGORY", str)

        assertEquals("BUDGET_CATEGORY", Converters.instance.stringFromBudgetType(null))
    }

    @Test
    fun testStringToMetadata() {
        val json = "{\"seen\":true}"
        val metadata = Converters.instance.stringToMetadata(json)
        assertNotNull(metadata)
        assertEquals(true, metadata?.get("seen")?.asBoolean)

        assertNull(Converters.instance.stringToMetadata(null))
    }

    @Test
    fun testStringFromMetadata() {
        val metadata = JsonObject().apply {
            addProperty("seen", true)
        }
        val json = Converters.instance.stringFromMetadata(metadata)
        assertEquals("{\"seen\":true}", json)
    }
}
