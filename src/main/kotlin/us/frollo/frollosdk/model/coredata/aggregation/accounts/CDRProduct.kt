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

package us.frollo.frollosdk.model.coredata.aggregation.accounts

import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName

/**
 * Data representation of CDR Product
 */
data class CDRProduct(

    /** Unique ID of the Product (optional) */
    @ColumnInfo(name = "product_id") @SerializedName("id") val productId: Long?,

    /** Name of the product (optional) */
    @ColumnInfo(name = "product_name") @SerializedName("name") val productName: String?,

    /** Contains a URL to a web page that displays more details about this Product (optional) */
    @ColumnInfo(name = "product_details_page_url") @SerializedName("product_details_page_url") val productDetailsPageUrl: String?,

    /** CDR Product Informations - Contains an ordered list of Key information about the CDR Product (optional) */
    @ColumnInfo(name = "key_information") @SerializedName("key_information") val cdrProductInformations: List<CDRProductInformation>?
)

/**
 * Data representation of CDR Product Information
 */
data class CDRProductInformation(

    /** Name of the CDR Product Information */
    @SerializedName("name") val name: String?,

    /** Value of the CDR Product Information */
    @SerializedName("value") val value: String?
)
