package us.frollo.frollosdk.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import us.frollo.frollosdk.extensions.fromJson
import us.frollo.frollosdk.model.coredata.aggregation.provideraccounts.AccountRefreshAdditionalStatus
import us.frollo.frollosdk.model.coredata.aggregation.provideraccounts.AccountRefreshStatus
import us.frollo.frollosdk.model.coredata.aggregation.provideraccounts.AccountRefreshSubStatus
import us.frollo.frollosdk.model.coredata.aggregation.providers.*
import us.frollo.frollosdk.model.coredata.messages.ContentType
import us.frollo.frollosdk.model.coredata.user.*
import java.util.*

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
    fun timestampToDate(value: Long?): Date? = if (value == null) null else Date(value)

    @TypeConverter
    fun timestampFromDate(date: Date?): Long? = date?.time

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
    fun stringToProviderEncryption(value: String?): ProviderEncryption? = if (value == null) null else gson.fromJson(value)

    @TypeConverter
    fun stringFromProviderEncryption(value: ProviderEncryption?): String? = if (value == null) null else gson.toJson(value)

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
}