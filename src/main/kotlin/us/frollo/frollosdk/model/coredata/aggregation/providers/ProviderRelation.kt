package us.frollo.frollosdk.model.coredata.aggregation.providers

import androidx.room.Embedded
import androidx.room.Relation
import us.frollo.frollosdk.model.IAdapterModel
import us.frollo.frollosdk.model.coredata.aggregation.provideraccounts.ProviderAccount

data class ProviderRelation(

        @Embedded
        var provider: Provider? = null,

        @Relation(parentColumn = "provider_id", entityColumn = "provider_id", entity = ProviderAccount::class)
        var providerAccounts: List<ProviderAccount>? = null

): IAdapterModel