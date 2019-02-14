package us.frollo.frollosdk.model.coredata.aggregation.accounts

import com.google.gson.annotations.SerializedName

data class HolderProfile(
    @SerializedName("name") val name: String
)