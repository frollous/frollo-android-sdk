package us.frollo.frollosdk.model.coredata.user

import com.google.gson.annotations.SerializedName

data class FeatureFlag(
        @SerializedName("feature") var feature: String,
        @SerializedName("enabled") var enabled: Boolean
)