package us.frollo.frollosdk.model.coredata.aggregation.accounts

import androidx.room.Embedded
import androidx.room.Relation
import us.frollo.frollosdk.model.IAdapterModel
import us.frollo.frollosdk.model.coredata.aggregation.provideraccounts.ProviderAccount
import us.frollo.frollosdk.model.coredata.aggregation.provideraccounts.ProviderAccountRelation
import us.frollo.frollosdk.model.coredata.aggregation.transactions.Transaction

data class AccountRelation(
        @Embedded
        var account: Account? = null,

        @Relation(parentColumn = "provider_account_id", entityColumn = "provider_account_id", entity = ProviderAccount::class)
        var providerAccounts: List<ProviderAccountRelation>? = null,

        @Relation(parentColumn = "account_id", entityColumn = "account_id", entity = Transaction::class)
        var transactions: List<Transaction>? = null
): IAdapterModel {

   val providerAccount: ProviderAccount?
       get() {
           val models = providerAccounts
           return if (models?.isNotEmpty() == true) models[0].providerAccount else null
       }
}