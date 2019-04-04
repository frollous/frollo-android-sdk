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

package us.frollo.frollosdk.model.coredata.reports

import androidx.room.*
import us.frollo.frollosdk.extensions.toBudgetCategory
import us.frollo.frollosdk.model.IAdapterModel
import us.frollo.frollosdk.model.coredata.aggregation.merchants.Merchant
import us.frollo.frollosdk.model.coredata.aggregation.transactioncategories.TransactionCategory
import us.frollo.frollosdk.model.coredata.shared.BudgetCategory

data class ReportGroupTransactionHistoryRelation(

        @Embedded
        var groupReport: ReportGroupTransactionHistory? = null,

        @Relation(parentColumn = "report_id", entityColumn = "report_id", entity = ReportTransactionHistory::class)
        var overallReports: List<ReportTransactionHistory>? = null,

        @Relation(parentColumn = "linked_id", entityColumn = "transaction_category_id", entity = TransactionCategory::class)
        var transactionCategories: List<TransactionCategory>? = null,

        @Relation(parentColumn = "linked_id", entityColumn = "merchant_id", entity = Merchant::class)
        var merchants: List<Merchant>? = null

): IAdapterModel {

        val overall: ReportTransactionHistory?
                get() {
                        val models = overallReports
                        return if (models?.isNotEmpty() == true) models[0] else null
                }

        val budgetCategory: BudgetCategory?
                get() {
                        return groupReport?.name?.toBudgetCategory()
                }

        val transactionCategory: TransactionCategory?
                get() {
                        val models = transactionCategories
                        return if (models?.isNotEmpty() == true) models[0] else null
                }

        val merchant: Merchant?
                get() {
                        val models = merchants
                        return if (models?.isNotEmpty() == true) models[0] else null
                }
}