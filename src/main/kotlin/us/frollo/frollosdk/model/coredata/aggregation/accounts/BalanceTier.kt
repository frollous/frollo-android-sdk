package us.frollo.frollosdk.model.coredata.aggregation.accounts

import com.google.gson.annotations.SerializedName

/** Represents what tier a certain balance on an account falls into. Used for [AccountType.CREDIT_SCORE] */
data class BalanceTier(

        /** Description */
        @SerializedName("description") val description: String,

        /** Minimum */
        @SerializedName("min") val min: Int,

        /** Maximum */
        @SerializedName("max") val max: Int
)