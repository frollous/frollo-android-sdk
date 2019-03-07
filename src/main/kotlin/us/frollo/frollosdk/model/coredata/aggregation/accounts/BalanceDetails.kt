package us.frollo.frollosdk.model.coredata.aggregation.accounts

import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName

/** Balance Details */
data class BalanceDetails(

        /** Current Description */
        @ColumnInfo(name = "current_description") @SerializedName("current_description") val currentDescription: String,

        /** Balance Tiers */
        @ColumnInfo(name = "tiers")  @SerializedName("tiers") val tiers: List<BalanceTier>
)