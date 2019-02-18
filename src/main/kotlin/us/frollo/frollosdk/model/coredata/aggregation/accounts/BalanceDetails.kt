package us.frollo.frollosdk.model.coredata.aggregation.accounts

import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName

data class BalanceDetails(
        @ColumnInfo(name = "current_description") @SerializedName("current_description") val currentDescription: String,
        @ColumnInfo(name = "tiers")  @SerializedName("tiers") val tiers: List<BalanceTier>
)