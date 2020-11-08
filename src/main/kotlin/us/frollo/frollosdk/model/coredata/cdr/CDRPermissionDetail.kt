package us.frollo.frollosdk.model.coredata.cdr

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.model.IAdapterModel

/** Represents detail for the CDR permission (Ex: Description of transactions) */
data class CDRPermissionDetail(

    /** The ID of the detail */
    @SerializedName("id") val detailId: String,

    /** The description for the detail */
    @SerializedName("description") val description: String

) : IAdapterModel
