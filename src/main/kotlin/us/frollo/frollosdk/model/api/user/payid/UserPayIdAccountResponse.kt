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

package us.frollo.frollosdk.model.api.user.payid

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.model.coredata.contacts.PayIDType
import us.frollo.frollosdk.model.coredata.user.payid.UserPayIdAccountStatus

/**
 * Data representation of User PayID Response configured on a given account.
 */
data class UserPayIdAccountResponse(

    /** The value of the payId */
    @SerializedName("payid") val payId: String,

    /** Current status of the PayID */
    @SerializedName("payid_status") val status: UserPayIdAccountStatus,

    /** The creditor PayID identifier type. */
    @SerializedName("type") val type: PayIDType,

    /** The name of the payID; shown to external parties */
    @SerializedName("payid_name") val name: String,

    /** The date and time the payID was registered */
    @SerializedName("created_at") val createdAt: String?,

    /** The date and time the payID was updated */
    @SerializedName("updated_at") val updatedAt: String?
)
