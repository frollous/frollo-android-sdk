package us.frollo.frollosdk.model.coredata.aggregation.accounts

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.extensions.serializedName

/** More detailed classification of the type of account */
enum class AccountClassification {

    /** Personal account */
    @SerializedName("personal") PERSONAL,

    /** Corporate account */
    @SerializedName("corporate") CORPORATE,

    /** Small business account */
    @SerializedName("small_business") SMALL_BUSINESS,

    /** Trust account */
    @SerializedName("trust") TRUST,

    /** Add on card account */
    @SerializedName("add_on_card") ADD_ON_CARD,

    /** Virtual card account */
    @SerializedName("virtual_card") VIRTUAL_CARD,

    /** Other account */
    @SerializedName("other") OTHER;

    /** Enum to serialized string */
    //This override MUST be used for this enum to work with Retrofit @Path or @Query parameters
    override fun toString(): String =
    //Try to get the annotation value if available instead of using plain .toString()
    //Fallback to super.toString() in case annotation is not present/available
            serializedName() ?: super.toString()
}