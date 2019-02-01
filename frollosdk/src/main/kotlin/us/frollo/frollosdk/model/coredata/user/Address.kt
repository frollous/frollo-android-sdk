package us.frollo.frollosdk.model.coredata.user

import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName

/**
 * Address of the user
 */
data class Address(
        /** Address first line of the user (optional) */
        @ColumnInfo(name = "line_1") @SerializedName("line_1") var lineOne: String? = null,
        /** Address second line of the user (optional) */
        @ColumnInfo(name = "line_2") @SerializedName("line_2") var lineTwo: String? = null,
        /** Suburb of the user (optional) */
        @ColumnInfo(name = "suburb") @SerializedName("suburb") var suburb: String? = null,
        /** Postcode (optional) */
        @ColumnInfo(name = "postcode") @SerializedName("postcode") var postcode: String? = null
)