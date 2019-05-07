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

package us.frollo.frollosdk.model.coredata.user

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import us.frollo.frollosdk.model.IAdapterModel

@Entity(tableName = "user_tags")
data class UserTags(

        @NonNull
        @ColumnInfo(name = "tag") var tag: String
) : IAdapterModel {

    /** Unique ID of the user tag */
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") var tagId: Long = 0
}