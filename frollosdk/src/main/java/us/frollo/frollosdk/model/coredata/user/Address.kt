package us.frollo.frollosdk.model.coredata.user

import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName

data class Address(
        @ColumnInfo(name = "line_1") @SerializedName("line_1") var lineOne: String? = null,
        @ColumnInfo(name = "line_2") @SerializedName("line_2") var lineTwo: String? = null,
        @ColumnInfo(name = "suburb") @SerializedName("suburb") var suburb: String? = null,
        @ColumnInfo(name = "postcode") @SerializedName("postcode") var postcode: String? = null
)