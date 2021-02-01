package us.frollo.frollosdk.model.api.contacts

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.model.coredata.contacts.ContactType

internal data class ContactCreateRequest(
    @SerializedName("name") val name: String,
    @SerializedName("nick_name") val nickName: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("payment_method") val paymentMethod: ContactType
)
