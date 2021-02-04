package us.frollo.frollosdk.model.api.contacts

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.model.coredata.contacts.PaymentDetails
import us.frollo.frollosdk.model.coredata.contacts.PaymentMethod

internal data class ContactCreateUpdateRequest(
    @SerializedName("name") val name: String,
    @SerializedName("nick_name") val nickName: String,
    @SerializedName("description") val description: String?,
    @SerializedName("payment_method") val paymentMethod: PaymentMethod,

    // DO NOT add @SerializedName("payment_details") to this field as it cannot be directly
    // serialized as PaymentDetails is abstract and hence we are using ContactRequestSerializer to initialize this field
    val paymentDetails: PaymentDetails? // NOTE: Any update to paymentDetails field ensure you update ContactRequestSerializer
)
