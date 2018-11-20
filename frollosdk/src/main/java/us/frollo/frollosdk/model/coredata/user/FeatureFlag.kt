package us.frollo.frollosdk.model.coredata.user

import com.google.gson.annotations.SerializedName

data class FeatureFlag(
        @SerializedName("feature") val feature: String,
        @SerializedName("enabled") val enabled: Boolean
)