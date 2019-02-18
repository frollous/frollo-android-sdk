package us.frollo.frollosdk.model.coredata.user

import com.google.gson.annotations.SerializedName

/**
 * User Attribution
 */
data class Attribution(
        /** Attribution network of the user (optional) */
        @SerializedName("network") var network: String? = null,
        /** Attribution campaign of the user (optional) */
        @SerializedName("campaign") var campaign: String? = null,
        /** Attribution creative of the user (optional) */
        @SerializedName("creative") var creative: String? = null,
        /** Attribution ad group of the user (optional) */
        @SerializedName("ad_group") var adGroup: String? = null
)