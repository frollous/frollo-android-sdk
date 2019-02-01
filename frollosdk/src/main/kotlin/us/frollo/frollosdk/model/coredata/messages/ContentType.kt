package us.frollo.frollosdk.model.coredata.messages

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.extensions.serializedName

/** Indicates the content type of the message and how it should be rendered */
enum class ContentType {
    /** The content contains text only and no image. Uses a standard design type */
    @SerializedName("text") TEXT,
    /** The content is HTML and should be rendered in a WebView */
    @SerializedName("html") HTML,
    /** The content contains a link to video content to be played */
    @SerializedName("video") VIDEO,
    /** The content contains an image. Fetch the image from the URL */
    @SerializedName("image") IMAGE;

    /** Enum to serialized string */
    //This override MUST be used for this enum to work with Retrofit @Path or @Query parameters
    override fun toString(): String =
    //Try to get the annotation value if available instead of using plain .toString()
    //Fallback to super.toString() in case annotation is not present/available
            serializedName() ?: super.toString()
}