package us.frollo.frollosdk.model.coredata.aggregation.providers

import androidx.room.Embedded
import androidx.room.Relation
import us.frollo.frollosdk.model.IAdapterModel
import us.frollo.frollosdk.model.coredata.aggregation.provideraccounts.ProviderAccount

/** Provider with associated data */
data class ProviderRelation(

        /** Provider */
        @Embedded
        var provider: Provider? = null,

        /** Associated Provider Accounts */
        @Relation(parentColumn = "provider_id", entityColumn = "provider_id", entity = ProviderAccount::class)
        var providerAccounts: List<ProviderAccount>? = null

): IAdapterModel