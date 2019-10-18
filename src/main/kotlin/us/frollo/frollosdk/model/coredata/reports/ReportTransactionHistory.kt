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

@Entity(tableName = "report_transaction_history",
        indices = [Index("report_id"),
            Index(value = ["date", "period", "filtered_budget_category", "report_grouping", "transaction_tags"], unique = true)])

/** Data representation of history transaction overall report */
data class ReportTransactionHistory(

    /** Date of the report period. Check [ReportDateFormat] for the date formats. */
    @ColumnInfo(name = "date") val date: String, // daily yyyy-MM-dd, monthly yyyy-MM, weekly yyyy-MM-W

    /** Value of the report */
    @ColumnInfo(name = "value") val value: BigDecimal,

    /** Budget value for the report (Optional) */
    @ColumnInfo(name = "budget") val budget: BigDecimal?,

    /** Period of the report */
    @ColumnInfo(name = "period") val period: ReportPeriod,

    /** Filter budget category if the report was filtered to a specific category */
    @ColumnInfo(name = "filtered_budget_category") val filteredBudgetCategory: BudgetCategory?,

    /** Transaction tags related to the report */
    @ColumnInfo(name = "transaction_tags") val transactionTags: List<String>?,

    /** Grouping - how the report response has been broken down */
    @ColumnInfo(name = "report_grouping") val grouping: ReportGrouping

) : IAdapterModel {

    /** Unique ID of the overall report */
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "report_id") var reportId: Long = 0
}