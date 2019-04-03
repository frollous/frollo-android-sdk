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

package us.frollo.frollosdk.model.api.reports

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

internal data class TransactionHistoryReportResponse(
        @SerializedName("data") val data: List<Report>
) {

    internal data class Report(
            @SerializedName("date") val date: String, // daily yyyy-MM-dd, monthly yyyy-MM, weekly yyyy-MM-W
            @SerializedName("value") val spendValue: BigDecimal,
            @SerializedName("budget") val budget: BigDecimal?,
            @SerializedName("groups") val groups: List<GroupReport>
    ) {

        internal data class GroupReport(
                @SerializedName("id") val id: Long,
                @SerializedName("name") val name: String,
                @SerializedName("budget") val budget: BigDecimal?,
                @SerializedName("value") val value: BigDecimal
        )
    }
}