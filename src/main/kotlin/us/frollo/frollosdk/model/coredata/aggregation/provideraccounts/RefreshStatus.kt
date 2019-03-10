/*
 * Copyright 2019 Frollo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package us.frollo.frollosdk.model.coredata.aggregation.provideraccounts

import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName

/** Refresh status */
data class RefreshStatus(

        /** Account refresh status */
        @ColumnInfo(name = "status") @SerializedName("status") var status: AccountRefreshStatus,

        /** Account refresh sub status */
        @ColumnInfo(name = "sub_status") @SerializedName("sub_status") val subStatus: AccountRefreshSubStatus?,

        /** Account refresh additional status */
        @ColumnInfo(name = "additional_status") @SerializedName("additional_status") val additionalStatus: AccountRefreshAdditionalStatus?,

        /** Date the aggregator last refreshed the provider account */
        @ColumnInfo(name = "last_refreshed") @SerializedName("last_refreshed") val lastRefreshed: String?, // ISO8601 format Eg: 2011-12-03T10:15:30+01:00

        /** Next refresh date by the aggregator */
        @ColumnInfo(name = "next_refresh") @SerializedName("next_refresh") val nextRefresh: String? // ISO8601 format Eg: 2011-12-03T10:15:30+01:00
)