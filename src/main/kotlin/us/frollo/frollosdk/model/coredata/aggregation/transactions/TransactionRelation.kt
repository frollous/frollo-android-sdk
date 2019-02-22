package us.frollo.frollosdk.model.coredata.aggregation.transactions

import androidx.room.Embedded
import androidx.room.Relation
import us.frollo.frollosdk.model.IAdapterModel
import us.frollo.frollosdk.model.coredata.aggregation.accounts.Account
import us.frollo.frollosdk.model.coredata.aggregation.accounts.AccountRelation
import us.frollo.frollosdk.model.coredata.aggregation.merchants.Merchant
import us.frollo.frollosdk.model.coredata.aggregation.transactioncategories.TransactionCategory

data class TransactionRelation(
        @Embedded
        var transaction: Transaction? = null,

        @Relation(parentColumn = "account_id", entityColumn = "account_id", entity = Account::class)
        var accounts: List<AccountRelation>? = null,

        @Relation(parentColumn = "category_id", entityColumn = "transaction_category_id", entity = TransactionCategory::class)
        var transactionCategories: List<TransactionCategory>? = null,

        @Relation(parentColumn = "merchant_id", entityColumn = "merchant_id", entity = Merchant::class)
        var merchants: List<Merchant>? = null
): IAdapterModel {

    val account: Account?
        get() {
            val models = accounts
            return if (models?.isNotEmpty() == true) models[0].account else null
        }

    val transactionCategory: TransactionCategory?
        get() {
            val models = transactionCategories
            return if (models?.isNotEmpty() == true) models[0] else null
        }

    val merchant: Merchant?
        get() {
            val models = merchants
            return if (models?.isNotEmpty() == true) models[0] else null
        }
}