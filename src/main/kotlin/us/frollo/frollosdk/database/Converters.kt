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

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.JsonObject
import us.frollo.frollosdk.extensions.fromJson
import us.frollo.frollosdk.model.coredata.aggregation.accounts.AccountClassification
import us.frollo.frollosdk.model.coredata.aggregation.accounts.AccountFeature
import us.frollo.frollosdk.model.coredata.aggregation.accounts.AccountFeatureDetail
import us.frollo.frollosdk.model.coredata.aggregation.accounts.AccountFeatureSubType
import us.frollo.frollosdk.model.coredata.aggregation.accounts.AccountFeatureType
import us.frollo.frollosdk.model.coredata.aggregation.accounts.AccountGroup
import us.frollo.frollosdk.model.coredata.aggregation.accounts.AccountStatus
import us.frollo.frollosdk.model.coredata.aggregation.accounts.AccountSubType
import us.frollo.frollosdk.model.coredata.aggregation.accounts.AccountType
import us.frollo.frollosdk.model.coredata.aggregation.accounts.BalanceTier
import us.frollo.frollosdk.model.coredata.aggregation.accounts.CDRProductInformation
import us.frollo.frollosdk.model.coredata.aggregation.merchants.MerchantLocation
import us.frollo.frollosdk.model.coredata.aggregation.merchants.MerchantType
import us.frollo.frollosdk.model.coredata.aggregation.provideraccounts.AccountRefreshAdditionalStatus
import us.frollo.frollosdk.model.coredata.aggregation.provideraccounts.AccountRefreshStatus
import us.frollo.frollosdk.model.coredata.aggregation.provideraccounts.AccountRefreshSubStatus
import us.frollo.frollosdk.model.coredata.aggregation.providers.AggregatorType
import us.frollo.frollosdk.model.coredata.aggregation.providers.ProviderAuthType
import us.frollo.frollosdk.model.coredata.aggregation.providers.ProviderContainerName
import us.frollo.frollosdk.model.coredata.aggregation.providers.ProviderEncryptionType
import us.frollo.frollosdk.model.coredata.aggregation.providers.ProviderLoginForm
import us.frollo.frollosdk.model.coredata.aggregation.providers.ProviderMFAType
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
import us.frollo.frollosdk.model.coredata.cdr.CDRPermission
import us.frollo.frollosdk.model.coredata.cdr.CDRPermissionDetail
import us.frollo.frollosdk.model.coredata.cdr.ConsentStatus
import us.frollo.frollosdk.model.coredata.cdr.SharingDuration
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
import us.frollo.frollosdk.model.coredata.user.Address
import us.frollo.frollosdk.model.coredata.user.Attribution
import us.frollo.frollosdk.model.coredata.user.FeatureFlag
import us.frollo.frollosdk.model.coredata.user.Gender
import us.frollo.frollosdk.model.coredata.user.HouseholdType
import us.frollo.frollosdk.model.coredata.user.Industry
import us.frollo.frollosdk.model.coredata.user.Occupation
import us.frollo.frollosdk.model.coredata.user.RegisterStep
import us.frollo.frollosdk.model.coredata.user.UserStatus
import java.math.BigDecimal

/**
 * Type converters to allow Room to reference complex data types.
 */
internal class Converters {

    companion object {
        val instance = Converters()
        private val gson = Gson()
    }

    // Generic
    @TypeConverter
    fun stringToListOfString(value: String?): List<String>? = value?.split("|")?.filter { it.isNotBlank() }

    @TypeConverter
    fun stringFromListOfString(value: List<String>?): String? = value?.joinToString(separator = "|", prefix = "|", postfix = "|")

    @TypeConverter
    fun stringToListOfLong(value: String?): List<Long>? = value?.split("|")?.filter { it.isNotBlank() }?.map { it.toLong() }

    @TypeConverter
    fun stringFromListOfLong(value: List<Long>?): String? = value?.joinToString(separator = "|", prefix = "|", postfix = "|")

    @TypeConverter
    fun stringToBigDecimal(value: String?): BigDecimal? = if (value == null) null else BigDecimal(value)

    @TypeConverter
    fun stringFromBigDecimal(value: BigDecimal?): String? = value?.toString()

    // User
    @TypeConverter
    fun stringToListOfFeatureFlag(value: String?): List<FeatureFlag>? = if (value == null) null else gson.fromJson<List<FeatureFlag>>(value)

    @TypeConverter
    fun stringFromListOfFeatureFlag(value: List<FeatureFlag>?): String? = if (value == null) null else gson.toJson(value)

    @TypeConverter
    fun stringToUserStatus(value: String?): UserStatus? = if (value == null) null else UserStatus.valueOf(value)

    @TypeConverter
    fun stringFromUserStatus(value: UserStatus?): String? = value?.name

    @TypeConverter
    fun stringToGender(value: String?): Gender? = if (value == null) null else Gender.valueOf(value)

    @TypeConverter
    fun stringFromGender(value: Gender?): String? = value?.name

    @TypeConverter
    fun stringToHouseholdType(value: String?): HouseholdType? = if (value == null) null else HouseholdType.valueOf(value)

    @TypeConverter
    fun stringFromHouseholdType(value: HouseholdType?): String? = value?.name

    @TypeConverter
    fun stringToOccupation(value: String?): Occupation? = if (value == null) null else Occupation.valueOf(value)

    @TypeConverter
    fun stringFromOccupation(value: Occupation?): String? = value?.name

    @TypeConverter
    fun stringToIndustry(value: String?): Industry? = if (value == null) null else Industry.valueOf(value)

    @TypeConverter
    fun stringFromIndustry(value: Industry?): String? = value?.name

    @TypeConverter
    fun stringToAttribution(value: String?): Attribution? = if (value == null) null else gson.fromJson(value)

    @TypeConverter
    fun stringFromAttribution(value: Attribution?): String? = if (value == null) null else gson.toJson(value)

    @TypeConverter
    fun stringToListOfRegisterStep(value: String?): List<RegisterStep>? = if (value == null) null else gson.fromJson<List<RegisterStep>>(value)

    @TypeConverter
    fun stringFromListOfRegisterStep(value: List<RegisterStep>?): String? = if (value == null) null else gson.toJson(value)

    @TypeConverter
    fun stringToAddress(value: String?): Address? = if (value == null) null else gson.fromJson(value)

    @TypeConverter
    fun stringFromAddress(value: Address?): String? = if (value == null) null else gson.toJson(value)

    // Message
    @TypeConverter
    fun stringToContentType(value: String?): ContentType? = if (value == null) ContentType.TEXT else ContentType.valueOf(value)

    @TypeConverter
    fun stringFromContentType(value: ContentType?): String? = value?.name ?: run { ContentType.TEXT.name }

    @TypeConverter
    fun stringToOpenMode(value: String?): OpenMode? = if (value == null) OpenMode.INTERNAL else OpenMode.valueOf(value)

    @TypeConverter
    fun stringFromOpenMode(value: OpenMode?): String? = value?.name ?: run { OpenMode.INTERNAL.name }

    // Aggregation

    // Provider
    @TypeConverter
    fun stringToProviderStatus(value: String?): ProviderStatus? = if (value == null) null else ProviderStatus.valueOf(value)

    @TypeConverter
    fun stringFromProviderStatus(value: ProviderStatus?): String? = value?.name

    @TypeConverter
    fun stringToProviderAuthType(value: String?): ProviderAuthType? = if (value == null) ProviderAuthType.UNKNOWN else ProviderAuthType.valueOf(value)

    @TypeConverter
    fun stringFromProviderAuthType(value: ProviderAuthType?): String? = value?.name ?: ProviderAuthType.UNKNOWN.name

    @TypeConverter
    fun stringToProviderMFAType(value: String?): ProviderMFAType? = if (value == null) ProviderMFAType.UNKNOWN else ProviderMFAType.valueOf(value)

    @TypeConverter
    fun stringFromProviderMFAType(value: ProviderMFAType?): String? = value?.name ?: ProviderMFAType.UNKNOWN.name

    @TypeConverter
    fun stringToProviderLoginForm(value: String?): ProviderLoginForm? = if (value == null) null else gson.fromJson(value)

    @TypeConverter
    fun stringFromProviderLoginForm(value: ProviderLoginForm?): String? = if (value == null) null else gson.toJson(value)

    @TypeConverter
    fun stringToProviderEncryptionType(value: String?): ProviderEncryptionType? = if (value == null) null else ProviderEncryptionType.valueOf(value)

    @TypeConverter
    fun stringFromProviderEncryptionType(value: ProviderEncryptionType?): String? = value?.name

    @TypeConverter
    fun stringToListOfProviderContainerName(value: String?): List<ProviderContainerName>? = value?.split("|")?.filter { it.isNotBlank() }?.map { ProviderContainerName.valueOf(it.toUpperCase()) }

    @TypeConverter
    fun stringFromListOfProviderContainerName(value: List<ProviderContainerName>?): String? = value?.joinToString(separator = "|", prefix = "|", postfix = "|")

    @TypeConverter
    fun stringToAggregatorType(value: String?): AggregatorType? = if (value == null || value.isEmpty() || value.isBlank()) AggregatorType.UNKNOWN else AggregatorType.valueOf(value)

    @TypeConverter
    fun stringFromAggregatorType(value: AggregatorType?): String? = value?.name ?: AggregatorType.UNKNOWN.name

    @TypeConverter
    fun stringToListOfCDRPermission(value: String?): List<CDRPermission>? = if (value == null) null else gson.fromJson<List<CDRPermission>>(value)

    @TypeConverter
    fun stringFromListOfCDRPermission(value: List<CDRPermission>?): String? = if (value == null) null else gson.toJson(value)

    @TypeConverter
    fun stringToListOfCDRPermissionDetail(value: String?): List<CDRPermissionDetail>? = if (value == null) null else gson.fromJson<List<CDRPermissionDetail>>(value)

    @TypeConverter
    fun stringFromListOfCDRPermissionDetail(value: List<CDRPermissionDetail>?): String? = if (value == null) null else gson.toJson(value)

    // ProviderAccount
    @TypeConverter
    fun stringToAccountRefreshStatus(value: String?): AccountRefreshStatus? = if (value == null) AccountRefreshStatus.UPDATING else AccountRefreshStatus.valueOf(value)

    @TypeConverter
    fun stringFromAccountRefreshStatus(value: AccountRefreshStatus?): String? = value?.name ?: AccountRefreshStatus.UPDATING.name

    @TypeConverter
    fun stringToAccountRefreshSubStatus(value: String?): AccountRefreshSubStatus? = if (value == null) null else AccountRefreshSubStatus.valueOf(value)

    @TypeConverter
    fun stringFromAccountRefreshSubStatus(value: AccountRefreshSubStatus?): String? = value?.name

    @TypeConverter
    fun stringToAccountRefreshAdditionalStatus(value: String?): AccountRefreshAdditionalStatus? = if (value == null) null else AccountRefreshAdditionalStatus.valueOf(value)

    @TypeConverter
    fun stringFromAccountRefreshAdditionalStatus(value: AccountRefreshAdditionalStatus?): String? = value?.name

    // Account
    @TypeConverter
    fun stringToAccountStatus(value: String?): AccountStatus? = if (value == null) null else AccountStatus.valueOf(value)

    @TypeConverter
    fun stringFromAccountStatus(value: AccountStatus?): String? = value?.name

    @TypeConverter
    fun stringToAccountType(value: String?): AccountType? = if (value == null) AccountType.UNKNOWN else AccountType.valueOf(value)

    @TypeConverter
    fun stringFromAccountType(value: AccountType?): String? = value?.name ?: AccountType.UNKNOWN.name

    @TypeConverter
    fun stringToAccountClassification(value: String?): AccountClassification? = if (value == null) AccountClassification.OTHER else AccountClassification.valueOf(value)

    @TypeConverter
    fun stringFromAccountClassification(value: AccountClassification?): String? = value?.name ?: AccountClassification.OTHER.name

    @TypeConverter
    fun stringToAccountSubType(value: String?): AccountSubType? = if (value == null) AccountSubType.OTHER else AccountSubType.valueOf(value)

    @TypeConverter
    fun stringFromAccountSubType(value: AccountSubType?): String? = value?.name ?: AccountSubType.OTHER.name

    @TypeConverter
    fun stringToAccountGroup(value: String?): AccountGroup? = if (value == null) AccountGroup.OTHER else AccountGroup.valueOf(value)

    @TypeConverter
    fun stringFromAccountGroup(value: AccountGroup?): String? = value?.name ?: AccountGroup.OTHER.name

    @TypeConverter
    fun stringToListOfBalanceTier(value: String?): List<BalanceTier>? = if (value == null) null else gson.fromJson<List<BalanceTier>>(value)

    @TypeConverter
    fun stringFromListOfBalanceTier(value: List<BalanceTier>?): String? = if (value == null) null else gson.toJson(value)

    @TypeConverter
    fun stringToListOfAccountFeature(value: String?): List<AccountFeature>? = if (value == null) null else gson.fromJson<List<AccountFeature>>(value)

    @TypeConverter
    fun stringFromListOfAccountFeature(value: List<AccountFeature>?): String? = if (value == null) null else gson.toJson(value)

    @TypeConverter
    fun stringToListOfAccountFeatureDetail(value: String?): List<AccountFeatureDetail>? = if (value == null) null else gson.fromJson<List<AccountFeatureDetail>>(value)

    @TypeConverter
    fun stringFromListOfAccountFeatureDetail(value: List<AccountFeatureDetail>?): String? = if (value == null) null else gson.toJson(value)

    @TypeConverter
    fun stringToAccountFeatureType(value: String?): AccountFeatureType? = if (value == null) AccountFeatureType.UNKNOWN else AccountFeatureType.valueOf(value)

    @TypeConverter
    fun stringFromAccountFeatureType(value: AccountFeatureType?): String? = value?.name ?: AccountFeatureType.UNKNOWN.name

    @TypeConverter
    fun stringToAccountFeatureSubType(value: String?): AccountFeatureSubType? = if (value == null) AccountFeatureSubType.UNKNOWN else AccountFeatureSubType.valueOf(value)

    @TypeConverter
    fun stringFromAccountFeatureSubType(value: AccountFeatureSubType?): String? = value?.name ?: AccountFeatureSubType.UNKNOWN.name

    @TypeConverter
    fun stringToListOfCDRProductInformation(value: String?): List<CDRProductInformation>? = if (value == null) null else gson.fromJson<List<CDRProductInformation>>(value)

    @TypeConverter
    fun stringFromListOfCDRProductInformation(value: List<CDRProductInformation>?): String? = if (value == null) null else gson.toJson(value)

    // Transaction

    @TypeConverter
    fun stringToTransactionBaseType(value: String?): TransactionBaseType? = if (value == null) TransactionBaseType.UNKNOWN else TransactionBaseType.valueOf(value)

    @TypeConverter
    fun stringFromTransactionBaseType(value: TransactionBaseType?): String? = value?.name ?: TransactionBaseType.UNKNOWN.name

    @TypeConverter
    fun stringToTransactionStatus(value: String?): TransactionStatus? = if (value == null) null else TransactionStatus.valueOf(value)

    @TypeConverter
    fun stringFromTransactionStatus(value: TransactionStatus?): String? = value?.name

    @TypeConverter
    fun stringToMerchantLocation(value: String?): MerchantLocation? = if (value == null) null else gson.fromJson(value)

    @TypeConverter
    fun stringFromMerchantLocation(value: MerchantLocation?): String? = if (value == null) null else gson.toJson(value)

    // Transaction Category

    @TypeConverter
    fun stringToTransactionCategoryType(value: String?): TransactionCategoryType? = if (value == null) TransactionCategoryType.UNCATEGORIZED else TransactionCategoryType.valueOf(value)

    @TypeConverter
    fun stringFromTransactionCategoryType(value: TransactionCategoryType?): String? = value?.name ?: TransactionCategoryType.UNCATEGORIZED.name

    // Merchant

    @TypeConverter
    fun stringToMerchantType(value: String?): MerchantType? = if (value == null) MerchantType.UNKNOWN else MerchantType.valueOf(value)

    @TypeConverter
    fun stringFromMerchantType(value: MerchantType?): String? = value?.name ?: MerchantType.UNKNOWN.name

    // Report

    @TypeConverter
    fun stringToReportGrouping(value: String?): ReportGrouping? = if (value == null) null else ReportGrouping.valueOf(value)

    @TypeConverter
    fun stringFromReportGrouping(value: ReportGrouping?): String? = value?.name

    @TypeConverter
    fun stringToReportPeriod(value: String?): ReportPeriod? = if (value == null) null else ReportPeriod.valueOf(value)

    @TypeConverter
    fun stringFromReportPeriod(value: ReportPeriod?): String? = value?.name

    // Bill

    @TypeConverter
    fun stringToBillType(value: String?): BillType? = if (value == null) BillType.BILL else BillType.valueOf(value)

    @TypeConverter
    fun stringFromBillType(value: BillType?): String? = value?.name ?: BillType.BILL.name

    @TypeConverter
    fun stringToBillStatus(value: String?): BillStatus? = if (value == null) BillStatus.ESTIMATED else BillStatus.valueOf(value)

    @TypeConverter
    fun stringFromBillStatus(value: BillStatus?): String? = value?.name ?: BillStatus.ESTIMATED.name

    @TypeConverter
    fun stringToBillFrequency(value: String?): BillFrequency? = if (value == null) BillFrequency.UNKNOWN else BillFrequency.valueOf(value)

    @TypeConverter
    fun stringFromBillFrequency(value: BillFrequency?): String? = value?.name ?: BillFrequency.UNKNOWN.name

    @TypeConverter
    fun stringToBillPaymentStatus(value: String?): BillPaymentStatus? = if (value == null) BillPaymentStatus.DUE else BillPaymentStatus.valueOf(value)

    @TypeConverter
    fun stringFromBillPaymentStatus(value: BillPaymentStatus?): String? = value?.name ?: BillPaymentStatus.DUE.name

    // Goal

    @TypeConverter
    fun stringToGoalTrackingType(value: String?): GoalTrackingType? = if (value == null) GoalTrackingType.DEBIT_CREDIT else GoalTrackingType.valueOf(value)

    @TypeConverter
    fun stringFromGoalTrackingType(value: GoalTrackingType?): String? = value?.name ?: GoalTrackingType.DEBIT_CREDIT.name

    @TypeConverter
    fun stringToGoalTrackingStatus(value: String?): GoalTrackingStatus? = if (value == null) GoalTrackingStatus.EQUAL else GoalTrackingStatus.valueOf(value)

    @TypeConverter
    fun stringFromGoalTrackingStatus(value: GoalTrackingStatus?): String? = value?.name ?: GoalTrackingStatus.EQUAL.name

    @TypeConverter
    fun stringToGoalTarget(value: String?): GoalTarget? = if (value == null) GoalTarget.OPEN_ENDED else GoalTarget.valueOf(value)

    @TypeConverter
    fun stringFromGoalTarget(value: GoalTarget?): String? = value?.name ?: GoalTarget.OPEN_ENDED.name

    @TypeConverter
    fun stringToGoalStatus(value: String?): GoalStatus? = if (value == null) GoalStatus.UNSTARTED else GoalStatus.valueOf(value)

    @TypeConverter
    fun stringFromGoalStatus(value: GoalStatus?): String? = value?.name ?: GoalStatus.UNSTARTED.name

    @TypeConverter
    fun stringToGoalFrequency(value: String?): GoalFrequency? = if (value == null) GoalFrequency.SINGULAR else GoalFrequency.valueOf(value)

    @TypeConverter
    fun stringFromGoalFrequency(value: GoalFrequency?): String? = value?.name ?: GoalFrequency.SINGULAR.name

    // Budget

    @TypeConverter
    fun stringFromBudgetTrackingStatus(value: BudgetTrackingStatus?): String? = value?.name ?: BudgetTrackingStatus.EQUAL.name

    @TypeConverter
    fun stringToBudgetTrackingStatus(value: String?): BudgetTrackingStatus? = if (value == null) BudgetTrackingStatus.EQUAL else BudgetTrackingStatus.valueOf(value)

    @TypeConverter
    fun stringFromBudgetStatus(value: BudgetStatus?): String? = value?.name ?: BudgetStatus.UNSTARTED.name

    @TypeConverter
    fun stringToBudgetStatus(value: String?): BudgetStatus? = if (value == null) BudgetStatus.UNSTARTED else BudgetStatus.valueOf(value)

    @TypeConverter
    fun stringFromBudgetFrequency(value: BudgetFrequency?): String? = value?.name ?: BudgetFrequency.MONTHLY.name

    @TypeConverter
    fun stringToBudgetFrequency(value: String?): BudgetFrequency? = if (value == null) BudgetFrequency.MONTHLY else BudgetFrequency.valueOf(value)

    @TypeConverter
    fun stringFromBudgetType(value: BudgetType?): String? = value?.name ?: BudgetType.BUDGET_CATEGORY.name

    @TypeConverter
    fun stringToBudgetType(value: String?): BudgetType? = if (value == null) BudgetType.BUDGET_CATEGORY else BudgetType.valueOf(value)

    // Shared

    @TypeConverter
    fun stringToBudgetCategory(value: String?): BudgetCategory? = if (value == null) null else BudgetCategory.valueOf(value)

    @TypeConverter
    fun stringFromBudgetCategory(value: BudgetCategory?): String? = value?.name

    @TypeConverter
    fun stringToMetadata(value: String?): JsonObject? = if (value == null) null else gson.fromJson(value)

    @TypeConverter
    fun stringFromMetadata(value: JsonObject?): String? = if (value == null) null else gson.toJson(value)

    // Consent

    @TypeConverter
    fun stringFromConsentStatus(value: ConsentStatus?): String? = value?.name ?: ConsentStatus.UNKNOWN.name

    @TypeConverter
    fun stringToConsentStatus(value: String?): ConsentStatus? = if (value == null) ConsentStatus.UNKNOWN else ConsentStatus.valueOf(value)

    @TypeConverter
    fun stringToListOfSharingDuration(value: String?): List<SharingDuration>? = if (value == null) null else gson.fromJson<List<SharingDuration>>(value)

    @TypeConverter
    fun stringFromListOfSharingDuration(value: List<SharingDuration>?): String? = if (value == null) null else gson.toJson(value)
}
