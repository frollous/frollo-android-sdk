package us.frollo.frollosdk.model.api.contacts

import com.google.gson.annotations.SerializedName

internal data class ContactRequest(

    @SerializedName("contact_id") val contactId: Long? = null
)
