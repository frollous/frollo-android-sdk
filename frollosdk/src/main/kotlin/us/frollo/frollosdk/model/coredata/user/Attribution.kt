package us.frollo.frollosdk.model.coredata.user

import com.google.gson.annotations.SerializedName

data class Attribution(
        @SerializedName("network") var network: String? = null,
        @SerializedName("campaign") var campaign: String? = null,
        @SerializedName("creative") var creative: String? = null,
        @SerializedName("ad_group") var adGroup: String? = null
)