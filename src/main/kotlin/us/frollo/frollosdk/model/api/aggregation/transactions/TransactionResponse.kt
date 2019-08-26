/*
 * Copyright 2019 Frollo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package us.frollo.frollosdk.model.api.aggregation.transactions

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.model.coredata.aggregation.accounts.Balance
import us.frollo.frollosdk.model.coredata.aggregation.merchants.MerchantDetails
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
    @SerializedName("merchant") val merchant: MerchantDetails,
    @SerializedName("bill_id") var billId: Long?,
    @SerializedName("bill_payment_id") var billPaymentId: Long?,
    @SerializedName("user_tags") val userTags: List<String>?,
    @SerializedName("external_id") val externalId: String
)