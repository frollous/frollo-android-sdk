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

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.model.IAdapterModel
import us.frollo.frollosdk.model.coredata.kyc.AddressType
import java.io.Serializable

/**
 * Address of the user
 */
data class Address(

    /** Address ID (Optional) */
    @SerializedName("address_id") var addressId: String? = null,
    /** Address type (Optional) */
    @SerializedName("address_type") var addressType: AddressType? = null,
    /** Address building name. (Optional) */
    @SerializedName("building_name") var buildingName: String? = null,
    /** Address unit number. (Optional) */
    @SerializedName("unit_number") var unitNumber: String? = null,
    /** Address street number. (Optional) */
    @SerializedName("street_number") var streetNumber: String? = null,
    /** Address street name. (Optional) */
    @SerializedName("street_name") var streetName: String? = null,
    /** Address street type. (Optional) */
    @SerializedName("street_type") var streetType: String? = null,
    /** Address suburb name. (Optional) */
    @SerializedName("suburb") var suburb: String? = null,
    /** Address town name. (Optional) */
    @SerializedName("town") var town: String? = null,
    /** Address region. (Optional) */
    @SerializedName("region") var region: String? = null,
    /** Address state. (Optional) */
    @SerializedName("state") var state: String? = null,
    /** Address country. (Optional) */
    @SerializedName("country") var country: String? = null,
    /** Address postcode. (Optional) */
    @SerializedName("postal_code") var postcode: String? = null,
    /** Full address in formatted form. (Optional) */
    @SerializedName("long_form") var longForm: String? = null

) : IAdapterModel, Serializable
