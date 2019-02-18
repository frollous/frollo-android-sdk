package us.frollo.frollosdk.model.coredata.aggregation.accounts

import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class Balance(
        @ColumnInfo(name = "amount") @SerializedName("amount") val amount: BigDecimal,
        @ColumnInfo(name = "currency") @SerializedName("currency") val currency: String
)