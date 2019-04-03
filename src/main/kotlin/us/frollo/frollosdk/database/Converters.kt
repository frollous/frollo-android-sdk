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
import us.frollo.frollosdk.extensions.fromJson
import us.frollo.frollosdk.model.coredata.aggregation.accounts.*
import us.frollo.frollosdk.model.coredata.aggregation.merchants.MerchantType
import us.frollo.frollosdk.model.coredata.aggregation.provideraccounts.AccountRefreshAdditionalStatus
import us.frollo.frollosdk.model.coredata.aggregation.provideraccounts.AccountRefreshStatus
import us.frollo.frollosdk.model.coredata.aggregation.provideraccounts.AccountRefreshSubStatus
import us.frollo.frollosdk.model.coredata.aggregation.providers.*
import us.frollo.frollosdk.model.coredata.aggregation.transactioncategories.TransactionCategoryType
import us.frollo.frollosdk.model.coredata.aggregation.transactions.TransactionBaseType
import us.frollo.frollosdk.model.coredata.aggregation.transactions.TransactionStatus
import us.frollo.frollosdk.model.coredata.messages.ContentType
import us.frollo.frollosdk.model.coredata.reports.ReportGrouping
import us.frollo.frollosdk.model.coredata.reports.ReportPeriod
import us.frollo.frollosdk.model.coredata.shared.BudgetCategory
import us.frollo.frollosdk.model.coredata.user.*
import java.math.BigDecimal

/**
 * Type converters to allow Room to reference complex data types.
 */
internal class Converters {

    companion object {
        val instance = Converters()
        private val gson = Gson()
    }

    //Generic
    @TypeConverter
    fun stringToListOfString(value: String?): List<String>? = if (value == null) null else value.split("|").filter { it.isNotBlank() }

    @TypeConverter
    fun stringFromListOfString(value: List<String>?): String? = if (value == null) null else value.joinToString(separator = "|", prefix = "|" , postfix = "|")

    @TypeConverter
    fun stringToListOfLong(value: String?): List<Long>? = if (value == null) null else value.split("|").filter { it.isNotBlank() }.map { it.toLong() }

    @TypeConverter
    fun stringFromListOfLong(value: List<Long>?): String? = if (value == null) null else value.joinToString(separator = "|", prefix = "|" , postfix = "|")

    @TypeConverter
    fun stringToBigDecimal(value: String?): BigDecimal? = if (value == null) null else BigDecimal(value)

    @TypeConverter
    fun stringFromBigDecimal(value: BigDecimal?): String? = value?.toString()

    //User
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

    //Message
    @TypeConverter
    fun stringToContentType(value: String?): ContentType? = if (value == null) ContentType.TEXT else ContentType.valueOf(value)

    @TypeConverter
    fun stringFromContentType(value: ContentType?): String? = value?.name ?: run { ContentType.TEXT.name }

    //Aggregation

    ///Provider
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
    fun stringToListOfProviderContainerName(value: String?): List<ProviderContainerName>? = if (value == null) null else value.split("|").filter { it.isNotBlank() }.map { ProviderContainerName.valueOf(it.toUpperCase()) }.toList()

    @TypeConverter
    fun stringFromListOfProviderContainerName(value: List<ProviderContainerName>?): String? = if (value == null) null else value.joinToString(separator = "|", prefix = "|" , postfix = "|")

    ///ProviderAccount
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

    ///Account
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

    // Transaction

    @TypeConverter
    fun stringToTransactionBaseType(value: String?): TransactionBaseType? = if (value == null) TransactionBaseType.UNKNOWN else TransactionBaseType.valueOf(value)

    @TypeConverter
    fun stringFromTransactionBaseType(value: TransactionBaseType?): String? = value?.name ?: TransactionBaseType.UNKNOWN.name

    @TypeConverter
    fun stringToTransactionStatus(value: String?): TransactionStatus? = if (value == null) null else TransactionStatus.valueOf(value)

    @TypeConverter
    fun stringFromTransactionStatus(value: TransactionStatus?): String? = value?.name

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

    // Shared

    @TypeConverter
    fun stringToBudgetCategory(value: String?): BudgetCategory? = if (value == null) null else BudgetCategory.valueOf(value)

    @TypeConverter
    fun stringFromBudgetCategory(value: BudgetCategory?): String? = value?.name
}