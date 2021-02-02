package us.frollo.frollosdk.model.coredata.contacts

import com.google.gson.annotations.SerializedName

/** Data representation of a International contact's bank address */
data class BankAddress(

    /** The address of the bank */
    @SerializedName("address") val address: String
)