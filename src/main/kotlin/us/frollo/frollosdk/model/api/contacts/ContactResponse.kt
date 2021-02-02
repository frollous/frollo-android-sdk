package us.frollo.frollosdk.model.api.contacts

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.model.coredata.contacts.PaymentDetails
import us.frollo.frollosdk.model.coredata.contacts.PaymentMethod

internal data class ContactResponse(
    @SerializedName("id") val contactId: Int,
    @SerializedName("created_date") val createdDate: String,
    @SerializedName("modified_date") val modifiedDate: String,
    @SerializedName("verified") val verified: Boolean,
    @SerializedName("related_provider_account_ids") val relatedProviderAccountIds: List<Long>?,
    @SerializedName("name") val name: String,
    @SerializedName("nick_name") val nickName: String,
    @SerializedName("description") val description: String?,
    @SerializedName("payment_method") val paymentMethod: PaymentMethod,
    @SerializedName("payment_details") val paymentDetails: PaymentDetails
)
