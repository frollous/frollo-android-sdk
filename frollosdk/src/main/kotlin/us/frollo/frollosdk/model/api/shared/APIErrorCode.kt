package us.frollo.frollosdk.model.api.shared

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.extensions.serializedName

enum class APIErrorCode {
    //400 Bad Request
    @SerializedName("F0001") INVALID_VALUE,
    @SerializedName("F0002") INVALID_LENGTH,
    @SerializedName("F0003") INVALID_AUTHORISATION_HEADER,
    @SerializedName("F0004") INVALID_USER_AGENT_HEADER,
    @SerializedName("F0005") INVALID_MUST_BE_DIFFERENT,
    @SerializedName("F0006") INVALID_OVER_LIMIT,
    @SerializedName("F0007") INVALID_COUNT,
    //401 Not authenticated
    @SerializedName("F0101") INVALID_ACCESS_TOKEN,
    @SerializedName("F0110") INVALID_REFRESH_TOKEN,
    @SerializedName("F0111") INVALID_USERNAME_PASSWORD,
    @SerializedName("F0112") SUSPENDED_USER,
    @SerializedName("F0113") SUSPENDED_DEVICE,
    @SerializedName("F0114") ACCOUNT_LOCKED,
    //403 Not authorised
    @SerializedName("F0200") UNAUTHORISED,
    //404 Object not found
    @SerializedName("F0300") NOT_FOUND,
    //409 Conflict
    @SerializedName("F0400") ALREADY_EXISTS,
    //500 Internal Server Error
    @SerializedName("F9000") AGGREGATOR_ERROR,
    @SerializedName("F9998") UNKNOWN_SERVER,
    @SerializedName("F9999") INTERNAL_EXCEPTION;

    //This override MUST be used for this enum to work with Retrofit @Path or @Query parameters
    override fun toString(): String =
    //Try to get the annotation value if available instead of using plain .toString()
    //Fallback to super.toString() in case annotation is not present/available
            serializedName() ?: super.toString()
}