package us.frollo.frollosdk.model.api.messages

import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName

internal data class MessageContent(
        @ColumnInfo(name = "main") @SerializedName("main") val main: String? = null,
        @ColumnInfo(name = "header") @SerializedName("header") val header: String? = null,
        @ColumnInfo(name = "footer") @SerializedName("footer") val footer: String? = null,

        @ColumnInfo(name = "text") @SerializedName("text") val text: String? = null,
        @ColumnInfo(name = "image_url") @SerializedName("image_url") val imageUrl: String? = null,
        @ColumnInfo(name = "design_type") @SerializedName("design_type") val designType: String? = null,

        @ColumnInfo(name = "url") @SerializedName("url") val url: String? = null,
        @ColumnInfo(name = "width") @SerializedName("width") val width: Double? = null,
        @ColumnInfo(name = "height") @SerializedName("height") val height: Double? = null,

        @ColumnInfo(name = "autoplay") @SerializedName("autoplay") val autoplay: Boolean? = null,
        @ColumnInfo(name = "autoplay_cellular") @SerializedName("autoplay_cellular") val autoplayCellular: Boolean? = null,
        @ColumnInfo(name = "icon_url") @SerializedName("icon_url") val iconUrl: String? = null,
        @ColumnInfo(name = "muted") @SerializedName("muted") val muted: Boolean? = null
)