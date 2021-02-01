package us.frollo.frollosdk.model.coredata.contacts

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.model.IAdapterModel
import java.io.Serializable

sealed class ContactDetailsType : IAdapterModel, Serializable {

    data class PayAnyone(

        /** The name of the account holder */
        @SerializedName("account_holder") val accountHolder: String,

        /** The bsb of the account holder */
        @SerializedName("bsb") val bsb: String,

        /** The account number of the account holder */
        @SerializedName("account_number") val accountNumber: String
    ) : ContactDetailsType()

    data class PayID(

        /** The payID of the contact */
        @SerializedName("payid") val payid: String,

        /** The name of the contact */
        @SerializedName("name") val name: String?,

        /** The pay id type of the contact */
        @SerializedName("id_type") val idType: ContactPayIdType

    ) : ContactDetailsType()

    data class Biller(

        /** The unique code of the biller */
        @SerializedName("biller_code") val billerCode: String,

        /** The customer reference number of the biller */
        @SerializedName("crn") val crn: String,

        /** The name of the biller */
        @SerializedName("biller_name") val billerName: String,

        /** The crn type of the biller */
        @SerializedName("crn_type") val crnType: ContactBillerCRNType
    ) : ContactDetailsType()

    data class International(

        /** The name of the contact */
        @SerializedName("name") val name: String?,

        /** The contry of the contact */
        @SerializedName("country") val country: String,

        /** The message from contact */
        @SerializedName("message") val message: String?,

        /** The name of the account holder */
        @SerializedName("bank_country") val bankCountry: String,

        /** The accountNumber of the contact */
        @SerializedName("account_number") val accountNumber: String,

        /** The bankAddress of the of the contact */
        @SerializedName("bank_address") val bankAddress: String?,

        /** The bic of the of the contact */
        @SerializedName("bic") val bic: String?,

        /** The fedWireNumber of the of the contact */
        @SerializedName("fed_wire_number") val fedwireNumber: String?,

        /** The sortCode of the of the contact */
        @SerializedName("sort_code") val sortCode: String?,

        /** The chipNumber of the of the contact */
        @SerializedName("chip_number") val chipNumber: String?,

        /** The routingNumber of the of the contact */
        @SerializedName("routing_number") val routingNumber: String?,

        /** The legalEntityIdentifier of the of the contact */
        @SerializedName("legal_entity_identifier") val legalEntityIdentifier: String?

    ) : ContactDetailsType()
}
