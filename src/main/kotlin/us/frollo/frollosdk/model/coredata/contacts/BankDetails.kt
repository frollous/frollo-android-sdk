package us.frollo.frollosdk.model.coredata.contacts

import com.google.gson.annotations.SerializedName

/** Data representation of a International contact's bank details */
data class BankDetails(

    /** The name of the account holder */
    @SerializedName("country") var country: String,

    /** The accountNumber of the contact */
    @SerializedName("account_number") var accountNumber: String,

    /** The bankAddress of the of the contact (Optional) */
    @SerializedName("bank_address") var bankAddress: BankAddress?,

    /** The bic of the of the contact (Optional) */
    @SerializedName("bic") var bic: String?,

    /** The fedWireNumber of the of the contact (Optional) */
    @SerializedName("fed_wire_number") var fedWireNumber: String?,

    /** The sortCode of the of the contact (Optional) */
    @SerializedName("sort_code") var sortCode: String?,

    /** The chipNumber of the of the contact (Optional) */
    @SerializedName("chip_number") var chipNumber: String?,

    /** The routingNumber of the of the contact (Optional) */
    @SerializedName("routing_number") var routingNumber: String?,

    /** The legalEntityIdentifier of the of the contact (Optional) */
    @SerializedName("legal_entity_identifier") var legalEntityIdentifier: String?
)
