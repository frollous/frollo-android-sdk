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

package us.frollo.frollosdk.model.coredata.aggregation.transactions

import us.frollo.frollosdk.model.coredata.aggregation.merchants.MerchantDetails
import us.frollo.frollosdk.model.coredata.shared.BudgetCategory

/**
 * Represents a model that contains all the filters to apply on transaction list
 *
 * @param transactionIds List of [Transaction.transactionId] to filter transactions
 * @param accountIds List of [Transaction.accountId] to filter transactions
 * @param budgetCategory [BudgetCategory] to filter transactions on
 * @param transactionCategoryIds List of [Transaction.categoryId] to filter transactions
 * @param merchantIds List of [MerchantDetails.id] to filter transactions
 * @param searchTerm Search term to filter transactions
 * @param minimumAmount Amount to filter transactions from (inclusive)
 * @param maximumAmount Amount to filter transactions to (inclusive)
 * @param baseType [TransactionBaseType] to filter transactions
 * @param tags List of tags to filter transactions
 * @param status [TransactionStatus] to filter transactions
 * @param fromDate Date to filter transactions from (inclusive). Please use [Transaction.DATE_FORMAT_PATTERN] for the format pattern.
 * @param toDate Date to filter transactions to (inclusive). Please use [Transaction.DATE_FORMAT_PATTERN] for the format pattern.
 * @param transactionIncluded [Transaction.included] status of 'Transaction' to filter by
 * @param accountIncluded 'included' status of 'Account' to filter by
 * @param after after field to get next list in pagination. Format is "<epoch_date>_<transaction_id>"
 * @param before before field to get previous list in pagination. Format is "<epoch_date>_<transaction_id>"
 * @param size Count of objects to returned from the API (page size)
 **/
data class TransactionFilter(
    var transactionIds: List<Long>? = null,
    var accountIds: List<Long>? = null,
    var budgetCategory: BudgetCategory? = null,
    var transactionCategoryIds: List<Long>? = null,
    var merchantIds: List<Long>? = null,
    var searchTerm: String? = null,
    var minimumAmount: String? = null,
    var maximumAmount: String? = null,
    var baseType: TransactionBaseType? = null,
    var tags: List<String>? = null,
    var status: TransactionStatus? = null,
    var fromDate: String? = null, // yyyy-MM-dd
    var toDate: String? = null, // yyyy-MM-dd
    var transactionIncluded: Boolean? = null,
    var accountIncluded: Boolean? = null,
    var after: String? = null,
    var before: String? = null,
    var size: Long? = null
)
