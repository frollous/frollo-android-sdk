package us.frollo.frollosdk.model.coredata.aggregation.accounts

import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

/** Balance model with amount and currency */
data class Balance(

        /** Amount */
        @ColumnInfo(name = "amount") @SerializedName("amount") val amount: BigDecimal,

        /** Currency */
        @ColumnInfo(name = "currency") @SerializedName("currency") val currency: String
)