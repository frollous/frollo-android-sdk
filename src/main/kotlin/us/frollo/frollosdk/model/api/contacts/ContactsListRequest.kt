package us.frollo.frollosdk.model.api.contacts

import com.google.gson.annotations.SerializedName

internal data class ContactsListRequest(

    @SerializedName("type") val name: String,
)
