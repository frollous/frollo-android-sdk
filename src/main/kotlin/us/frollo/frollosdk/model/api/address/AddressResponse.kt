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

package us.frollo.frollosdk.model.api.address

import com.google.gson.annotations.SerializedName

internal data class AddressResponse(
    @SerializedName("id") val addressId: Long,
    @SerializedName("building_name") val buildingName: String? = null,
    @SerializedName("unit_number") val unitNumber: String? = null,
    @SerializedName("street_number") val streetNumber: String? = null,
    @SerializedName("street_name") val streetName: String? = null,
    @SerializedName("street_type") val streetType: String? = null,
    @SerializedName("suburb") val suburb: String? = null,
    @SerializedName("town") val town: String? = null,
    @SerializedName("region") val region: String? = null,
    @SerializedName("state") val state: String? = null,
    @SerializedName("country") val country: String? = null,
    @SerializedName("postal_code") val postcode: String? = null,
    @SerializedName("long_form") val longForm: String? = null
)
