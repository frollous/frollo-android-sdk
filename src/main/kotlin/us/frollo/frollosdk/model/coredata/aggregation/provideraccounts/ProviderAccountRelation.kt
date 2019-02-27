package us.frollo.frollosdk.model.coredata.aggregation.provideraccounts

import androidx.room.Embedded
import androidx.room.Relation
import us.frollo.frollosdk.model.IAdapterModel
import us.frollo.frollosdk.model.coredata.aggregation.accounts.Account
import us.frollo.frollosdk.model.coredata.aggregation.providers.Provider

data class ProviderAccountRelation(

        @Embedded
        var providerAccount: ProviderAccount? = null,

        @Relation(parentColumn = "provider_id", entityColumn = "provider_id", entity = Provider::class)
        var providers: List<Provider>? = null,

        @Relation(parentColumn = "provider_account_id", entityColumn = "provider_account_id", entity = Account::class)
        var accounts: List<Account>? = null

): IAdapterModel {

    val provider: Provider?
        get() {
            val models = providers
            return if (models?.isNotEmpty() == true) models[0] else null
        }
}