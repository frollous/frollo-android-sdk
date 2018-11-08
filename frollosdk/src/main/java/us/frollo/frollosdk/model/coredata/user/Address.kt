package us.frollo.frollosdk.model.coredata.user

import com.google.gson.annotations.SerializedName

data class Address(
        @SerializedName("line_1") var lineOne: String?,
        @SerializedName("line_2") var lineTwo: String?,
        @SerializedName("suburb") var suburb: String?,
        @SerializedName("postcode") var postcode: String?
)