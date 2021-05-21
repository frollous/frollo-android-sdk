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

package us.frollo.frollosdk.model.coredata.payday

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// Declaring the ColumnInfo allows for the renaming of variables without
// implementing a database migration, as the column name would not change.

@Entity(tableName = "payday")
/**
 * Data representation of Payday
 */
data class Payday(

    /** Unique ID of the card */
    @ColumnInfo(name = "status") val status: PaydayStatus,

    /** ID of the account to which the card is associated with */
    @ColumnInfo(name = "frequency") val frequency: PaydayFrequency,

    /** Next pay date (Optional). See [Payday.DATE_FORMAT_PATTERN] for the date format pattern */
    @ColumnInfo(name = "next_date") val nextDate: String?, // yyyy-MM-dd

    /** Last pay date (Optional). See [Payday.DATE_FORMAT_PATTERN] for the date format pattern */
    @ColumnInfo(name = "previous_date") val previousDate: String?, // yyyy-MM-dd

) {
    /** Unique ID of the payday */
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "payday_id") var paydayId: Long = 0

    companion object {

        /** Date format for dates associated with Payday */
        const val DATE_FORMAT_PATTERN = "yyyy-MM-dd"
    }
}
