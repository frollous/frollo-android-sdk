package us.frollo.frollosdk.model.coredata.messages

import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName

data class Action(
        @ColumnInfo(name = "title") @SerializedName("title") var title: String? = null,
        @ColumnInfo(name = "link") @SerializedName("link") var link: String? = null,
        @ColumnInfo(name = "open_external") @SerializedName("open_external") var openExternal: Boolean)