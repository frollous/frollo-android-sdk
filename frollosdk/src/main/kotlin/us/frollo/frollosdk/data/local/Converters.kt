package us.frollo.frollosdk.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import us.frollo.frollosdk.extensions.fromJson
import us.frollo.frollosdk.model.coredata.messages.ContentType
import us.frollo.frollosdk.model.coredata.user.*

/**
 * Type converters to allow Room to reference complex data types.
 */
internal class Converters {

    companion object {
        val instance = Converters()
        private val gson = Gson()
    }

    @TypeConverter
    fun stringToListOfString(value: String?): List<String>? = if (value == null) null else value.split("|").filter { it.isNotBlank() }

    @TypeConverter
    fun stringFromListOfString(value: List<String>?): String? = if (value == null) null else value.joinToString(separator = "|", prefix = "|" , postfix = "|")

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
    fun stringToContentType(value: String?): ContentType? = if (value == null) ContentType.TEXT else ContentType.valueOf(value)

    @TypeConverter
    fun stringFromContentType(value: ContentType?): String? = value?.name ?: run { ContentType.TEXT.name }
}