package us.frollo.frollosdk.model.api.contacts

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.model.coredata.contacts.PaymentMethod

internal data class ContactInternationalCreateUpdateRequest(
    @SerializedName("name") val name: String,
    @SerializedName("nick_name") val nickName: String,
    @SerializedName("description") val description: String?,
    @SerializedName("payment_method") val paymentMethod: PaymentMethod = PaymentMethod.INTERNATIONAL,
    @SerializedName("payment_details") val paymentDetails: InternationalPaymentDetails
) {
    internal data class InternationalPaymentDetails(
        @SerializedName("name") val name: String?,
        @SerializedName("country") val country: String,
        @SerializedName("message") val message: String?,
        @SerializedName("bank_country") val bankCountry: String,
        @SerializedName("account_number") val accountNumber: String,
        @SerializedName("bank_address") val bankAddress: String?,
        @SerializedName("bic") val bic: String?,
        @SerializedName("fed_wire_number") val fedWireNumber: String?,
        @SerializedName("sort_code") val sortCode: String?,
        @SerializedName("chip_number") val chipNumber: String?,
        @SerializedName("routing_number") val routingNumber: String?,
        @SerializedName("legal_entity_identifier") val legalEntityIdentifier: String?
    )
}