package us.frollo.frollosdk.model.coredata.contacts

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/** Represents the payment details of a contact */
/* NOTE: Any update to PaymentDetails ensure you update ContactDeserializer & ContactSerializer */
sealed class PaymentDetails : Serializable {

    /** Represents the payment details of a PayAnyone contact */
    data class PayAnyone(
        /** The name of the account holder */
        @SerializedName("account_holder") var accountHolder: String,

        /** The BSB of the account holder */
        @SerializedName("bsb") var bsb: String,

        /** The account number of the account holder */
        @SerializedName("account_number") var accountNumber: String
    ) : PaymentDetails()

    /** Represents the payment details of a BPay contact */
    data class Biller(
        /** The unique code of the biller */
        @SerializedName("biller_code") var billerCode: String,

        /** The customer reference number of the biller */
        @SerializedName("crn") var crn: String,

        /** The name of the biller */
        @SerializedName("biller_name") var billerName: String,

        /** The CRN type of the biller */
        @SerializedName("crn_type") var crnType: CRNType
    ) : PaymentDetails()

    /** Represents the payment details of a PayID contact */
    data class PayID(
        /** The payID of the contact */
        @SerializedName("payid") var payId: String,

        /** The name of the contact (Optional) */
        @SerializedName("name") var name: String?,

        /** The payId type of the contact */
        @SerializedName("id_type") var idType: PayIDType
    ) : PaymentDetails()

    /** Represents the payment details of a International Payment contact */
    data class International(

        /** Beneficiary details of the contact */
        @SerializedName("beneficiary") val beneficiary: Beneficiary,

        /** Bank details of the contact */
        @SerializedName("bank_details") val bankDetails: BankDetails
    ) : PaymentDetails()

    companion object {
        internal fun jsonIsPayAnyone(json: String): Boolean {
            return json.contains("account_number") && json.contains("bsb")
        }
        internal fun jsonIsBiller(json: String): Boolean {
            return json.contains("biller_code") && json.contains("crn")
        }
        internal fun jsonIsPayID(json: String): Boolean {
            return json.contains("payid") && json.contains("id_type")
        }
        internal fun jsonIsInternational(json: String): Boolean {
            return json.contains("beneficiary") && json.contains("bank_details")
        }
    }
}
