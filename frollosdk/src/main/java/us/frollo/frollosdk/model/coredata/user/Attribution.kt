package us.frollo.frollosdk.model.coredata.user

import com.google.gson.annotations.SerializedName

data class Attribution(
        @SerializedName("network") var network: String?,
        @SerializedName("campaign") var campaign: String?,
        @SerializedName("creative") var creative: String?,
        @SerializedName("ad_group") var adGroup: String?
)