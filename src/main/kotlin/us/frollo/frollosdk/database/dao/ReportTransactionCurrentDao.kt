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

package us.frollo.frollosdk.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import us.frollo.frollosdk.model.coredata.reports.ReportTransactionCurrent
import us.frollo.frollosdk.model.coredata.reports.ReportTransactionCurrentRelation

@Dao
internal interface ReportTransactionCurrentDao {

    @androidx.room.Transaction
    @Query("SELECT * FROM report_transaction_current")
    fun load(): LiveData<ReportTransactionCurrentRelation?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg models: ReportTransactionCurrent): LongArray

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(model: ReportTransactionCurrent): Long

    @Query("DELETE FROM report_transaction_current")
    fun clear()
}