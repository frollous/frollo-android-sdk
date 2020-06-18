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

package us.frollo.frollosdk.model.coredata.aggregation.merchants

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import us.frollo.frollosdk.model.IAdapterModel

// Declaring the ColumnInfo allows for the renaming of variables without
// implementing a database migration, as the column name would not change.

@Entity(
    tableName = "merchant",
    indices = [Index("merchant_id")]
)

/** Data representation of a Merchant */
data class Merchant(

    /** Unique ID for the merchant */
    @PrimaryKey @ColumnInfo(name = "merchant_id") val merchantId: Long,

    /** Name of the merchant */
    @ColumnInfo(name = "name") val name: String,

    /** Type of merchant */
    @ColumnInfo(name = "merchant_type") val merchantType: MerchantType,

    /** URL of the merchant's small logo image */
    @ColumnInfo(name = "small_logo_url") val smallLogoUrl: String?

) : IAdapterModel
