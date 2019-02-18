package us.frollo.frollosdk.model.api.aggregation.transactions

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.model.coredata.shared.BudgetCategory

internal data class TransactionUpdateRequest(
        @SerializedName("budget_category") val budgetCategory: BudgetCategory,
        @SerializedName("category_id") val categoryId: Long? = null,
        @SerializedName("included") val included: Boolean? = null,
        @SerializedName("memo") val memo: String? = null,
        @SerializedName("user_description") val userDescription: String? = null,
        @SerializedName("recategorise_all") val recategoriseAll: Boolean? = null,
        @SerializedName("include_apply_all") val includeApplyAll: Boolean? = null
)