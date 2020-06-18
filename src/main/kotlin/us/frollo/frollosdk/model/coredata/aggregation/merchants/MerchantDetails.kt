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
import com.google.gson.annotations.SerializedName

/** Merchant details */
data class MerchantDetails(

    /** Unique ID of the merchant */
    @ColumnInfo(name = "id") @SerializedName("id") val id: Long,

    /** Merchant name */
    @ColumnInfo(name = "name") @SerializedName("name") var name: String,

    /** Merchant phone number (optional) */
    @ColumnInfo(name = "phone") @SerializedName("phone") val phone: String?,

    /** Merchant website (optional) */
    @ColumnInfo(name = "website") @SerializedName("website") val website: String?,

    /** Merchant location (optional) */
    @ColumnInfo(name = "location") @SerializedName("location") val location: MerchantLocation?
)
