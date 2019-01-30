package us.frollo.frollosdk.model.coredata.messages

import com.google.gson.annotations.SerializedName

enum class ContentType {
    @SerializedName("text") TEXT,
    @SerializedName("html") HTML,
    @SerializedName("video") VIDEO,
    @SerializedName("image") IMAGE,
}