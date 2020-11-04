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

package us.frollo.frollosdk.model

import us.frollo.frollosdk.model.coredata.cdr.CDRPermission
import us.frollo.frollosdk.model.coredata.cdr.CDRPermissionDetail

internal fun testCDRPermissionData(): List<CDRPermission> {
    return listOf(
        CDRPermission(
            permissionId = "account_details",
            title = "Account balance and details",
            description = "We leverage...",
            required = true,
            details = listOf(
                CDRPermissionDetail(
                    detailId = "account_name",
                    description = "Name of account"
                )
            )
        ),
        CDRPermission(
            permissionId = "transaction_details",
            title = "Transaction and details",
            description = "We leverage...",
            required = false,
            details = listOf(
                CDRPermissionDetail(
                    detailId = "transaction_name",
                    description = "Name of transaction"
                )
            )
        )
    )
}
