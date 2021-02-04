package us.frollo.frollosdk.model.api.contacts

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.model.coredata.contacts.PaymentDetails
import us.frollo.frollosdk.model.coredata.contacts.PaymentMethod

internal data class ContactResponse(
    @SerializedName("id") val contactId: Long,
    @SerializedName("created_date") val createdDate: String,
    @SerializedName("modified_date") val modifiedDate: String,
    @SerializedName("verified") val verified: Boolean,
    @SerializedName("related_provider_account_ids") val relatedProviderAccountIds: List<Long>?,
    @SerializedName("name") val name: String,
    @SerializedName("nick_name") val nickName: String,
    @SerializedName("description") val description: String?,
    @SerializedName("payment_method") val paymentMethod: PaymentMethod,

    // DO NOT add @SerializedName("payment_details") to this field as it cannot be directly
    // de-serialized as PaymentDetails is abstract and hence we are using ContactResponseDeserializer to initialize this field
    var paymentDetails: PaymentDetails? // NOTE: Any update to paymentDetails field ensure you update ContactResponseDeserializer
)
