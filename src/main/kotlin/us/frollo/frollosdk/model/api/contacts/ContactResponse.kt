package us.frollo.frollosdk.model.api.contacts

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.model.coredata.contacts.ContactDetailsType
import us.frollo.frollosdk.model.coredata.contacts.ContactType

data class ContactResponse(

    @SerializedName("id") val contactId: Int,
    @SerializedName("created_date") val createdDate: String,
    @SerializedName("modified_date") val modifiedDate: String,
    @SerializedName("verified") val verified: Boolean,
    @SerializedName("related_provider_account_ids") val providerAccountIds: List<Int>,
    @SerializedName("name") val name: String,
    @SerializedName("nick_name") val nickName: String,
    @SerializedName("description") val description: String?,
    @SerializedName("payment_method") val contactType: ContactType,
    @SerializedName("payment_details")val contactDetailsType: ContactDetailsType
)
