package us.frollo.frollosdk.model.coredata.aggregation.provideraccounts

import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.model.coredata.aggregation.provideraccounts.AccountRefreshAdditionalStatus
import us.frollo.frollosdk.model.coredata.aggregation.provideraccounts.AccountRefreshStatus
import us.frollo.frollosdk.model.coredata.aggregation.provideraccounts.AccountRefreshSubStatus
import java.util.*

data class RefreshStatus(
        @ColumnInfo(name = "status") @SerializedName("status") var status: AccountRefreshStatus,
        @ColumnInfo(name = "sub_status") @SerializedName("sub_status") val subStatus: AccountRefreshSubStatus?,
        @ColumnInfo(name = "additional_status") @SerializedName("additional_status") val additionalStatus: AccountRefreshAdditionalStatus?,
        @ColumnInfo(name = "last_refreshed") @SerializedName("last_refreshed") val lastRefreshed: Date?,
        @ColumnInfo(name = "next_refresh") @SerializedName("next_refresh") val nextRefresh: Date?
)