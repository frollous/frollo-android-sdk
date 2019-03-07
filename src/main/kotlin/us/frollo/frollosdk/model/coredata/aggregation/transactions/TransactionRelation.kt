package us.frollo.frollosdk.model.coredata.aggregation.transactions

import androidx.room.Embedded
import androidx.room.Relation
import us.frollo.frollosdk.model.IAdapterModel
import us.frollo.frollosdk.model.coredata.aggregation.accounts.Account
import us.frollo.frollosdk.model.coredata.aggregation.accounts.AccountRelation
import us.frollo.frollosdk.model.coredata.aggregation.merchants.Merchant
import us.frollo.frollosdk.model.coredata.aggregation.transactioncategories.TransactionCategory

/** Transaction with associated data */
data class TransactionRelation(

        /** Transaction */
        @Embedded
        var transaction: Transaction? = null,

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
        @Relation(parentColumn = "merchant_id", entityColumn = "merchant_id", entity = Merchant::class)
        var merchants: List<Merchant>? = null

): IAdapterModel {

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