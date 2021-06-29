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

package us.frollo.frollosdk.model

import us.frollo.frollosdk.model.api.address.AddressResponse
import us.frollo.frollosdk.testutils.randomNumber
import us.frollo.frollosdk.testutils.randomString

internal fun testAddressResponseData(addressId: Long? = null): AddressResponse {
    return AddressResponse(
        addressId = addressId ?: randomNumber().toLong(),
        buildingName = randomString(20),
        unitNumber = randomString(20),
        streetNumber = randomString(20),
        streetName = randomString(20),
        streetType = randomString(20),
        suburb = randomString(20),
        town = randomString(20),
        region = randomString(20),
        state = randomString(20),
        country = randomString(20),
        postcode = randomString(20),
        longForm = randomString(50)
    )
}
