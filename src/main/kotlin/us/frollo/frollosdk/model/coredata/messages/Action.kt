package us.frollo.frollosdk.model.coredata.messages

import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName

/** Action data in a message */
data class Action(
        /** Title of the action (optional) */
        @ColumnInfo(name = "title") @SerializedName("title") var title: String? = null,
        /** Raw value of the action URL (optional) */
        @ColumnInfo(name = "link") @SerializedName("link") var link: String? = null,
        /** Action should open the link externally or internally. Externally means the system should handle opening the link. */
        @ColumnInfo(name = "open_external") @SerializedName("open_external") var openExternal: Boolean)