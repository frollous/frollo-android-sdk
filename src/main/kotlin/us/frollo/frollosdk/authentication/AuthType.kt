package us.frollo.frollosdk.authentication

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.extensions.serializedName

/**
 * The method to be used for authenticating the user when logging in.
 */
enum class AuthType {
    /**
     * Authenticate with an email address and password
     */
    @SerializedName("email") EMAIL,
    /**
     * Authenticate using Facebook using the user's email, Facebook User ID and Facebook Access Token.
     */
    @SerializedName("facebook") FACEBOOK,
    /**
     * Authenticate using a Volt token, requires email, Volt user ID and Volt access token.
     */
    @SerializedName("volt") VOLT;

    /**
     * Converts enum to serialized string value.
     */
    //This override MUST be used for this enum to work with Retrofit @Path or @Query parameters
    override fun toString(): String =
    //Try to get the annotation value if available instead of using plain .toString()
    //Fallback to super.toString() in case annotation is not present/available
            serializedName() ?: super.toString()
}