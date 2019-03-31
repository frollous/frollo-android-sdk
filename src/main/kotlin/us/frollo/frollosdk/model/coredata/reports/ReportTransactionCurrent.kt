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

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import us.frollo.frollosdk.model.IAdapterModel
import us.frollo.frollosdk.model.coredata.shared.BudgetCategory
import java.math.BigDecimal

// Declaring the ColumnInfo allows for the renaming of variables without
// implementing a database migration, as the column name would not change.

@Entity(tableName = "report_transaction_current",
        indices = [Index("report_id"),
            Index(value = ["day", "filtered_budget_category", "report_grouping"], unique = true)])

data class ReportTransactionCurrent(
        @ColumnInfo(name = "day") val day: Int,
        @ColumnInfo(name = "spend_value") val amount: BigDecimal?,
        @ColumnInfo(name = "previous_period_value") val previous: BigDecimal?,
        @ColumnInfo(name = "average_value") val average: BigDecimal?,
        @ColumnInfo(name = "budget_value") val budget: BigDecimal?,
        @ColumnInfo(name = "filtered_budget_category") val filteredBudgetCategory: BudgetCategory?,
        @ColumnInfo(name = "report_grouping") val grouping: ReportGrouping

): IAdapterModel {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "report_id") var reportId: Long? = null
    fun getId(): Long? = reportId

}