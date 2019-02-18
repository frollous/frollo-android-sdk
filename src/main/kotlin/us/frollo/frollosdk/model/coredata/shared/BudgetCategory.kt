package us.frollo.frollosdk.model.coredata.shared

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.extensions.serializedName

enum class BudgetCategory {
    @SerializedName("income") INCOME,
    @SerializedName("living") LIVING,
    @SerializedName("lifestyle") LIFESTYLE,
    @SerializedName("goals") GOALS,
    @SerializedName("one_off") ONE_OFF;

    //This override MUST be used for this enum to work with Retrofit @Path or @Query parameters
    override fun toString(): String =
    //Try to get the annotation value if available instead of using plain .toString()
    //Fallback to super.toString() in case annotation is not present/available
            serializedName() ?: super.toString()
}