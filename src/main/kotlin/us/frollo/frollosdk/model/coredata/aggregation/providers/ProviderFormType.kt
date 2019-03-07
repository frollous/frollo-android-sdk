package us.frollo.frollosdk.model.coredata.aggregation.providers

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.extensions.serializedName

/** Indicates what the form is requesting information for */
enum class ProviderFormType {

    /** Image, for example captchas to be entered by the user */
    @SerializedName("image") IMAGE,

    /** Login form for linking an initial account */
    @SerializedName("login") LOGIN,

    /** Question and answer, security questions asked as part of the MFA process */
    @SerializedName("questionAndAnswer") QUESTION_AND_ANSWER,

    /** Token requesting a OTP or token code to be entered as part of the MFA process */
    @SerializedName("token") TOKEN;

    /** Enum to serialized string */
    //This override MUST be used for this enum to work with Retrofit @Path or @Query parameters
    override fun toString(): String =
    //Try to get the annotation value if available instead of using plain .toString()
    //Fallback to super.toString() in case annotation is not present/available
            serializedName() ?: super.toString()
}