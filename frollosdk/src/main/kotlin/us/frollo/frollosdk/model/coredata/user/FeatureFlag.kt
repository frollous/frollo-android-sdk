package us.frollo.frollosdk.model.coredata.user

import com.google.gson.annotations.SerializedName

/**
 * Represents features which are available to the user
 */
data class FeatureFlag(
        /** Feature name */
        @SerializedName("feature") val feature: String,
        /** Feature enabled or disabled */
        @SerializedName("enabled") val enabled: Boolean
)