package us.frollo.frollosdk.model.api.messages

import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName

internal data class MessageContent(
        @ColumnInfo(name = "main") @SerializedName("main") var main: String? = null,
        @ColumnInfo(name = "header") @SerializedName("header") var header: String? = null,
        @ColumnInfo(name = "footer") @SerializedName("footer") var footer: String? = null,

        @ColumnInfo(name = "text") @SerializedName("text") var text: String? = null,
        @ColumnInfo(name = "image_url") @SerializedName("image_url") var imageUrl: String? = null,
        @ColumnInfo(name = "design_type") @SerializedName("design_type") var designType: String? = null,

        @ColumnInfo(name = "url") @SerializedName("url") var url: String? = null,
        @ColumnInfo(name = "width") @SerializedName("width") var width: Double? = null,
        @ColumnInfo(name = "height") @SerializedName("height") var height: Double? = null,

        @ColumnInfo(name = "autoplay") @SerializedName("autoplay") var autoplay: Boolean? = null,
        @ColumnInfo(name = "autoplay_cellular") @SerializedName("autoplay_cellular") var autoplayCellular: Boolean? = null,
        @ColumnInfo(name = "icon_url") @SerializedName("icon_url") var iconUrl: String? = null,
        @ColumnInfo(name = "muted") @SerializedName("muted") var muted: Boolean? = null
)