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

package us.frollo.frollosdk.model.coredata.aggregation.tags

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import us.frollo.frollosdk.model.IAdapterModel

@Entity(tableName = "transaction_user_tags",
        indices = [Index("name")])

/** Data representation of a TransactionTag */
data class TransactionTag(

    /** Unique ID of the TransactionTag & name of the tag */
    @PrimaryKey @ColumnInfo(name = "name") val name: String,

    /** Number of times the tag has been used */
    @ColumnInfo(name = "count") val count: Long?,

    /**
     * Date format for this field is ISO8601
     * example 2011-12-03T10:15:30+01:00
     * */
    @ColumnInfo(name = "last_used_at") val lastUsedAt: String?,

    /**
     * Earliest date the tag was last used on
     * Date format for this field is ISO8601
     * example 2011-12-03T10:15:30+01:00
     * */
    @ColumnInfo(name = "created_at") val createdAt: String?

) : IAdapterModel