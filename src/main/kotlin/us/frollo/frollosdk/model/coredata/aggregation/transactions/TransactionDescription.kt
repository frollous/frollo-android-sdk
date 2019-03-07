package us.frollo.frollosdk.model.coredata.aggregation.transactions

import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName

/** Transaction description */
data class TransactionDescription(

        /** Original description of the transaction */
        @ColumnInfo(name = "original") @SerializedName("original") val original: String,

        /** User determined description of the transaction (optional) */
        @ColumnInfo(name = "user") @SerializedName("user") var user: String?,

        /** Simplified description of the transaction (optional) */
        @ColumnInfo(name = "simple") @SerializedName("simple") val simple: String?
)