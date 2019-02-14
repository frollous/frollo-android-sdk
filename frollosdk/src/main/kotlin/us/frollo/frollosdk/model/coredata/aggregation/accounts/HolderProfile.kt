package us.frollo.frollosdk.model.coredata.aggregation.accounts

import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName

data class HolderProfile(
        @ColumnInfo(name = "name") @SerializedName("name") val name: String
)