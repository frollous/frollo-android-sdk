package us.frollo.frollosdk.model.coredata.aggregation.transactions

import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName

data class TransactionDescription(
        @ColumnInfo(name = "original") @SerializedName("original") val original: String,
        @ColumnInfo(name = "user") @SerializedName("user") var user: String?,
        @ColumnInfo(name = "simple") @SerializedName("simple") val simple: String?
)