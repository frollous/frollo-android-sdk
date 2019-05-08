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
import androidx.sqlite.db.SupportSQLiteQuery
import us.frollo.frollosdk.model.api.messages.MessageResponse
import us.frollo.frollosdk.model.coredata.aggregation.tags.TransactionTag

@Dao
internal interface TransactionUserTagsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(tagsList: List<TransactionTag>): LongArray

    @Query("DELETE FROM transaction_user_tags WHERE name NOT IN (:tagNames)")
    fun deleteByNamesInverse(tagNames: List<String>)

    @Query("SELECT * FROM transaction_user_tags")
    fun load(): LiveData<List<TransactionTag>>

    @RawQuery(observedEntities = [TransactionTag::class])
    fun custom(queryStr: SupportSQLiteQuery): LiveData<List<TransactionTag>>
}