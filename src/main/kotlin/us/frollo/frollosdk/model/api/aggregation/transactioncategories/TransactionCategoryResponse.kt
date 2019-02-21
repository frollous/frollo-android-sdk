package us.frollo.frollosdk.model.api.aggregation.transactioncategories

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.model.coredata.aggregation.transactioncategories.TransactionCategoryType
import us.frollo.frollosdk.model.coredata.shared.BudgetCategory

internal data class TransactionCategoryResponse(
        @SerializedName("id") val transactionCategoryId: Long,
        @SerializedName("user_defined") val userDefined: Boolean,
        @SerializedName("name") val name: String,
        @SerializedName("category_type") val categoryType: TransactionCategoryType,
        @SerializedName("default_budget_category") val defaultBudgetCategory: BudgetCategory,
        @SerializedName("icon_url") val iconUrl: String,
        @SerializedName("placement") val placement: Int
)