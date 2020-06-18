/*
 * Copyright 2019 Frollo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
