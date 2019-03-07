package us.frollo.frollosdk.model.coredata.aggregation.provideraccounts

import androidx.room.Embedded
import androidx.room.Relation
import us.frollo.frollosdk.model.IAdapterModel
import us.frollo.frollosdk.model.coredata.aggregation.accounts.Account
import us.frollo.frollosdk.model.coredata.aggregation.providers.Provider

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
        var accounts: List<Account>? = null

): IAdapterModel {

    /** Associated Provider */
    val provider: Provider?
        get() {
            val models = providers
            return if (models?.isNotEmpty() == true) models[0] else null
        }
}