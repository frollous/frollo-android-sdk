package us.frollo.frollosdk.model.coredata.aggregation.accounts

import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName

/** Account holder profile */
data class HolderProfile(

        /** Name of the account holder */
        @ColumnInfo(name = "name") @SerializedName("name") val name: String
)