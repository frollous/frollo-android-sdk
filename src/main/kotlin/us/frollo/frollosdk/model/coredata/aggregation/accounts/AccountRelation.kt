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

import androidx.room.Embedded
import androidx.room.Relation
import us.frollo.frollosdk.model.IAdapterModel
import us.frollo.frollosdk.model.coredata.aggregation.provideraccounts.ProviderAccount
import us.frollo.frollosdk.model.coredata.aggregation.provideraccounts.ProviderAccountRelation
import us.frollo.frollosdk.model.coredata.aggregation.transactions.Transaction

/** Account with associated data */
data class AccountRelation(

        /** Account */
        @Embedded
        var account: Account? = null,

        /** Associated Provider Account
         *
         * Even though its a list this will have only one element. It is requirement of Room database for this to be a list.
         */
        @Relation(parentColumn = "provider_account_id", entityColumn = "provider_account_id", entity = ProviderAccount::class)
        var providerAccounts: List<ProviderAccountRelation>? = null,

        /** Associated Transactions */
        @Relation(parentColumn = "account_id", entityColumn = "account_id", entity = Transaction::class)
        var transactions: List<Transaction>? = null

): IAdapterModel {

    /** Associated Provider Account */
    val providerAccount: ProviderAccountRelation?
        get() {
            val models = providerAccounts
            return if (models?.isNotEmpty() == true) models[0] else null
        }
}