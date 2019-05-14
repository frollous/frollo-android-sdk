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
            Index(value = ["linked_id", "day", "filtered_budget_category", "report_grouping"], unique = true)])

/** Data representation of current transaction report */
data class ReportTransactionCurrent(

    /** Day of the month */
    @ColumnInfo(name = "day") val day: Int,

    /** Unique ID of the related object. E.g. merchant or category. null value represents no link or overall report */
    @ColumnInfo(name = "linked_id") val linkedId: Long?,

    /** Name of the related object (Optional) */
    @ColumnInfo(name = "linked_name") val name: String?,

    /** Spend */
    @ColumnInfo(name = "spend_value") val amount: BigDecimal?,

    /** Previous month spend */
    @ColumnInfo(name = "previous_period_value") val previous: BigDecimal?,

    /** Average amount from last 3 months */
    @ColumnInfo(name = "average_value") val average: BigDecimal?,

    /** Budgeted amount (Optional) */
    @ColumnInfo(name = "budget_value") val budget: BigDecimal?,

    /** Filter Budget Category - indicates the Budget Category the reports were filtered by (Optional) */
    @ColumnInfo(name = "filtered_budget_category") val filteredBudgetCategory: BudgetCategory?,

    /** Grouping - how the report response has been broken down */
    @ColumnInfo(name = "report_grouping") val grouping: ReportGrouping

) : IAdapterModel {

    /** Unique ID of the report */
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "report_id") var reportId: Long = 0
}