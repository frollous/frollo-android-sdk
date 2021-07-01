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

import androidx.room.Embedded
import androidx.room.Relation
import us.frollo.frollosdk.model.IAdapterModel
import us.frollo.frollosdk.model.coredata.address.Address

/** User with associated data */
data class UserRelation(

    /** User */
    @Embedded
    var user: User? = null,

    /** Associated Residential Address
     *
     * Even though its a list this will have only one element. It is requirement of Room database for this to be a list.
     */
    @Relation(parentColumn = "residential_address_id", entityColumn = "address_id", entity = Address::class)
    var residentialAddresses: List<Address>? = null,

    /** Associated Mailing Address
     *
     * Even though its a list this will have only one element. It is requirement of Room database for this to be a list.
     */
    @Relation(parentColumn = "mailing_address_id", entityColumn = "address_id", entity = Address::class)
    var mailingAddresses: List<Address>? = null,

    /** Associated Previous Address
     *
     * Even though its a list this will have only one element. It is requirement of Room database for this to be a list.
     */
    @Relation(parentColumn = "previous_address_id", entityColumn = "address_id", entity = Address::class)
    var previousAddresses: List<Address>? = null

) : IAdapterModel {

    /** Associated Residential Address */
    val residentialAddress: Address?
        get() {
            val models = residentialAddresses
            return if (models?.isNotEmpty() == true) models[0] else null
        }

    /** Associated Mailing Address */
    val mailingAddress: Address?
        get() {
            val models = mailingAddresses
            return if (models?.isNotEmpty() == true) models[0] else null
        }

    /** Associated Previous Address */
    val previousAddress: Address?
        get() {
            val models = previousAddresses
            return if (models?.isNotEmpty() == true) models[0] else null
        }
}
