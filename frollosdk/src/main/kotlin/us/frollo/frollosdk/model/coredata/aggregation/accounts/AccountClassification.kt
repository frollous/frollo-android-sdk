package us.frollo.frollosdk.model.coredata.aggregation.accounts

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.extensions.serializedName

enum class AccountClassification {
    @SerializedName("personal") PERSONAL,
    @SerializedName("corporate") CORPORATE,
    @SerializedName("small_business") SMALL_BUSINESS,
    @SerializedName("trust") TRUST,
    @SerializedName("add_on_card") ADD_ON_CARD,
    @SerializedName("virtual_card") VIRTUAL_CARD,
    @SerializedName("other") OTHER;

    //This override MUST be used for this enum to work with Retrofit @Path or @Query parameters
    override fun toString(): String =
    //Try to get the annotation value if available instead of using plain .toString()
    //Fallback to super.toString() in case annotation is not present/available
            serializedName() ?: super.toString()
}