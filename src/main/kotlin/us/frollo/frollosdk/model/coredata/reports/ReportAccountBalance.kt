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
import java.math.BigDecimal

// Declaring the ColumnInfo allows for the renaming of variables without
// implementing a database migration, as the column name would not change.

@Entity(tableName = "report_account_balance",
        indices = [Index("report_id"),
            Index(value = ["account_id", "date", "period"], unique = true)])

/** Data representation of account balance report */
data class ReportAccountBalance(

    /** Date of the report period. Check [ReportDateFormat] for the date formats. */
    @ColumnInfo(name = "date") val date: String, // daily yyyy-MM-dd, monthly yyyy-MM, weekly yyyy-MM-W

    /** Related account ID */
    @ColumnInfo(name = "account_id") val accountId: Long,

    /** Currency of the report. ISO 4217 code */
    @ColumnInfo(name = "currency") val currency: String,

    /** Balance of the report */
    @ColumnInfo(name = "value") val value: BigDecimal,

    /** Period of the report */
    @ColumnInfo(name = "period") val period: ReportPeriod

) : IAdapterModel {

    /** Unique ID of the report */
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "report_id") var reportId: Long = 0
}