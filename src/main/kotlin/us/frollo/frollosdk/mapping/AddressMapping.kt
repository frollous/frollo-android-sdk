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

package us.frollo.frollosdk.mapping

import us.frollo.frollosdk.model.api.address.AddressResponse
import us.frollo.frollosdk.model.coredata.address.Address

internal fun AddressResponse.toAddress(): Address =
    Address(
        addressId = addressId,
        unitNumber = unitNumber,
        buildingName = buildingName,
        streetNumber = streetNumber,
        streetName = streetName,
        streetType = streetType,
        suburb = suburb,
        town = town,
        region = region,
        state = state,
        country = country,
        postcode = postcode,
        longForm = longForm
    )
