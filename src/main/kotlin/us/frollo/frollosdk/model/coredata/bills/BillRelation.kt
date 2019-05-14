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

package us.frollo.frollosdk.model.coredata.bills

import androidx.room.Embedded
import androidx.room.Relation
import us.frollo.frollosdk.model.IAdapterModel
import us.frollo.frollosdk.model.coredata.aggregation.accounts.Account
import us.frollo.frollosdk.model.coredata.aggregation.accounts.AccountRelation
import us.frollo.frollosdk.model.coredata.aggregation.merchants.Merchant
import us.frollo.frollosdk.model.coredata.aggregation.transactioncategories.TransactionCategory

/** Bill with associated data */
data class BillRelation(

    /** Bill */
    @Embedded
    var bill: Bill? = null,

    /** Associated Bill Payments */
    @Relation(parentColumn = "bill_id", entityColumn = "bill_id", entity = BillPayment::class)
    var payments: List<BillPayment>? = null,

    /** Associated Account
     *
     * Even though its a list this will have only one element. It is requirement of Room database for this to be a list.
     */
    @Relation(parentColumn = "account_id", entityColumn = "account_id", entity = Account::class)
    var accounts: List<AccountRelation>? = null,

    /** Associated Transaction Category
     *
     * Even though its a list this will have only one element. It is requirement of Room database for this to be a list.
     */
    @Relation(parentColumn = "category_id", entityColumn = "transaction_category_id", entity = TransactionCategory::class)
    var transactionCategories: List<TransactionCategory>? = null,

        /** Associated Merchant
         *
         * Even though its a list this will have only one element. It is requirement of Room database for this to be a list.
         */
    /**  */
    @Relation(parentColumn = "merchant_id", entityColumn = "merchant_id", entity = Merchant::class)
    var merchants: List<Merchant>? = null

) : IAdapterModel {

    /** Associated Account */
    val account: AccountRelation?
        get() {
            val models = accounts
            return if (models?.isNotEmpty() == true) models[0] else null
        }

    /** Associated Transaction Category */
    val transactionCategory: TransactionCategory?
        get() {
            val models = transactionCategories
            return if (models?.isNotEmpty() == true) models[0] else null
        }

    /** Associated Merchant */
    val merchant: Merchant?
        get() {
            val models = merchants
            return if (models?.isNotEmpty() == true) models[0] else null
        }
}