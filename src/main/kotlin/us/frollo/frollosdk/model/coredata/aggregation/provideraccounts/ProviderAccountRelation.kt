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

package us.frollo.frollosdk.model.coredata.aggregation.provideraccounts

import androidx.room.Embedded
import androidx.room.Relation
import us.frollo.frollosdk.model.IAdapterModel
import us.frollo.frollosdk.model.coredata.aggregation.accounts.Account
import us.frollo.frollosdk.model.coredata.aggregation.providers.Provider
import us.frollo.frollosdk.model.coredata.cdr.Consent

/** Provider Account with associated data */
data class ProviderAccountRelation(

    /** Provider Account */
    @Embedded
    var providerAccount: ProviderAccount? = null,

    /** Associated Provider
     *
     * Even though its a list this will have only one element. It is requirement of Room database for this to be a list.
     */
    @Relation(parentColumn = "provider_id", entityColumn = "provider_id", entity = Provider::class)
    var providers: List<Provider>? = null,

    /** Associated Accounts */
    @Relation(parentColumn = "provider_account_id", entityColumn = "provider_account_id", entity = Account::class)
    var accounts: List<Account>? = null,

    /** Associated Consents */
    @Relation(parentColumn = "provider_account_id", entityColumn = "provider_account_id", entity = Consent::class)
    var consents: List<Consent>? = null

) : IAdapterModel {

    /** Associated Provider */
    val provider: Provider?
        get() {
            val models = providers
            return if (models?.isNotEmpty() == true) models[0] else null
        }

    /** Associated Consent */
    val consent: Consent?
        get() {
            val models = consents
            return if (models?.isNotEmpty() == true) models[0] else null
        }
}
