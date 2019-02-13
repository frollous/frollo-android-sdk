package us.frollo.frollosdk.model.coredata.aggregation.provideraccounts

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.model.coredata.aggregation.provideraccounts.AccountRefreshAdditionalStatus
import us.frollo.frollosdk.model.coredata.aggregation.provideraccounts.AccountRefreshStatus
import us.frollo.frollosdk.model.coredata.aggregation.provideraccounts.AccountRefreshSubStatus
import java.util.*

data class ProviderAccountRefreshStatus(
        @SerializedName("status") var status: AccountRefreshStatus,
        @SerializedName("sub_status") val subStatus: AccountRefreshSubStatus?,
        @SerializedName("additional_status") val additionalStatus: AccountRefreshAdditionalStatus?,
        @SerializedName("last_refreshed") val lastRefreshed: Date?,
        @SerializedName("next_refresh") val nextRefresh: Date?
)