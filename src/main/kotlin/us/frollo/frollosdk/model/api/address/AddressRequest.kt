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

internal data class AddressRequest(
    @SerializedName("building_name") var buildingName: String? = null,
    @SerializedName("unit_number") var unitNumber: String? = null,
    @SerializedName("street_number") var streetNumber: String? = null,
    @SerializedName("street_name") var streetName: String? = null,
    @SerializedName("street_type") var streetType: String? = null,
    @SerializedName("suburb") var suburb: String? = null,
    @SerializedName("town") var town: String? = null,
    @SerializedName("region") var region: String? = null,
    @SerializedName("state") var state: String? = null,
    @SerializedName("country") var country: String,
    @SerializedName("postal_code") var postcode: String? = null
)
