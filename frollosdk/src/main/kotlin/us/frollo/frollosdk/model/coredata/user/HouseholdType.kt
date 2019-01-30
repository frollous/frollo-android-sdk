package us.frollo.frollosdk.model.coredata.user

import com.google.gson.annotations.SerializedName

enum class HouseholdType {
    @SerializedName("single") SINGLE,
    @SerializedName("single_parent") SINGLE_PARENT,
    @SerializedName("couple") COUPLE,
    @SerializedName("couple_with_kids") COUPLE_WITH_KIDS,
}