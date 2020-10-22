package us.frollo.frollosdk.model.coredata.aggregation.cdr

import com.google.gson.annotations.SerializedName

/** Represents detail for the CDR permission (Ex: Description of transactions) */
data class CDRPermissionDetail(

    /** The ID of the detail */
    @SerializedName("id") val detailId: String,

    /** The description for the detail */
    @SerializedName("description") val description: String
)
