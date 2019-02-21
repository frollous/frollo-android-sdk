package us.frollo.frollosdk.model.api.aggregation.transactions

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.model.coredata.aggregation.accounts.Balance
import us.frollo.frollosdk.model.coredata.aggregation.transactions.TransactionBaseType
import us.frollo.frollosdk.model.coredata.aggregation.transactions.TransactionDescription
import us.frollo.frollosdk.model.coredata.aggregation.transactions.TransactionStatus
import us.frollo.frollosdk.model.coredata.shared.BudgetCategory

internal data class TransactionResponse(
        @SerializedName("id") val transactionId: Long,
        @SerializedName("base_type") val baseType: TransactionBaseType,
        @SerializedName("status") val status: TransactionStatus,
        @SerializedName("transaction_date") val transactionDate: String, // yyyy-MM-dd
        @SerializedName("post_date") val postDate: String?, // yyyy-MM-dd
        @SerializedName("amount") val amount: Balance,
        @SerializedName("description") var description: TransactionDescription?,
        @SerializedName("budget_category") var budgetCategory: BudgetCategory,
        @SerializedName("included") var included: Boolean,
        @SerializedName("memo") var memo: String?,
        @SerializedName("account_id") val accountId: Long,
        @SerializedName("category_id") var categoryId: Long,
        @SerializedName("merchant_id") val merchantId: Long,
        @SerializedName("bill_id") var billId: Long?,
        @SerializedName("bill_payment_id") var billPaymentId: Long?
)