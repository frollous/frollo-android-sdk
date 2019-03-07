package us.frollo.frollosdk.model.oauth

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.extensions.serializedName

internal enum class OAuthGrantType {
    @SerializedName("authorization_code") AUTHORIZATION_CODE,
    @SerializedName("password") PASSWORD,
    @SerializedName("refresh_token") REFRESH_TOKEN;

    //This override MUST be used for this enum to work with Retrofit @Path or @Query parameters
    override fun toString(): String =
            //Try to get the annotation value if available instead of using plain .toString()
            //Fallback to super.toString() in case annotation is not present/available
            serializedName() ?: super.toString()
}
