package us.frollo.frollosdk.model.coredata.aggregation.provideraccounts

import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName

data class RefreshStatus(
        @ColumnInfo(name = "status") @SerializedName("status") var status: AccountRefreshStatus,
        @ColumnInfo(name = "sub_status") @SerializedName("sub_status") val subStatus: AccountRefreshSubStatus?,
        @ColumnInfo(name = "additional_status") @SerializedName("additional_status") val additionalStatus: AccountRefreshAdditionalStatus?,
        @ColumnInfo(name = "last_refreshed") @SerializedName("last_refreshed") val lastRefreshed: String?, // ISO8601 format Eg: 2011-12-03T10:15:30+01:00
        @ColumnInfo(name = "next_refresh") @SerializedName("next_refresh") val nextRefresh: String? // ISO8601 format Eg: 2011-12-03T10:15:30+01:00
)