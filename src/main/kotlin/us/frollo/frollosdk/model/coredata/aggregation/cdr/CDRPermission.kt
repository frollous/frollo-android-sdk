package us.frollo.frollosdk.model.coredata.aggregation.cdr

import com.google.gson.annotations.SerializedName

/** Represent a CDR permission (Ex: Transaction Details) */
data class CDRPermission(

    /** The ID of the permission */
    @SerializedName("id") val permissionId: String,

    /** The title of the permission */
    @SerializedName("title") val title: String,

    /** The description of the permission */
    @SerializedName("description") val description: String,

    /** Specifies whether this permission is required or not */
    @SerializedName("required") val required: Boolean,

    /** The details of the permission */
    @SerializedName("details") val details: List<CDRPermissionDetail>
)
