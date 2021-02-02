package us.frollo.frollosdk.model.coredata.contacts

import com.google.gson.annotations.SerializedName

/** Data representation of a International contact's beneficiary details */
data class Beneficiary(

    /** The name of the International contact (Optional) */
    @SerializedName("name") var name: String?,

    /** The country of the International contact */
    @SerializedName("country") var country: String,

    /** The message from International contact (Optional) */
    @SerializedName("message") var message: String?
)
