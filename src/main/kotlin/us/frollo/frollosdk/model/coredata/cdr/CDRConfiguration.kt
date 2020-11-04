/*
 * Copyright 2020 Frollo
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

package us.frollo.frollosdk.model.coredata.cdr

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

// Declaring the ColumnInfo allows for the renaming of variables without
// implementing a database migration, as the column name would not change.

@Entity(
    tableName = "cdr_configuration",
    indices = [Index("adr_id")]
)

/** Represents key information of CDR Product */
data class CDRConfiguration(

    /** The ID of the ADR that handles CDR data */
    @PrimaryKey @ColumnInfo(name = "adr_id") @SerializedName("adr_id") val adrId: String,

    /** The name of the ADR that handles CDR data */
    @ColumnInfo(name = "adr_name") @SerializedName("adr_name") val adrName: String,

    /** The email to contact for support */
    @ColumnInfo(name = "support_email") @SerializedName("support_email") val supportEmail: String,

    /** The sharing durations for the CDR configuration */
    @ColumnInfo(name = "sharing_durations") @SerializedName("sharing_durations") val sharingDurations: List<SharingDuration>
)
