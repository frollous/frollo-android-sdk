/*
 * Copyright 2020 Frollo
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

package us.frollo.frollosdk.model.api.cdr

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.model.coredata.cdr.CDRPermission

internal data class ConsentCreateRequest(

    /** The ID for the provider */
    @SerializedName("provider_id") val providerId: Long,

    /** The duration (in seconds) for the consent */
    @SerializedName("sharing_duration") val sharingDuration: Long,

    /** List of permission IDs requested for the consent. Refer to [CDRPermission.permissionId] */
    @SerializedName("permissions") val permissions: List<String>,

    /** Additional permissions (meta-data map of String:Boolean) that can be set (Optional) */
    @SerializedName("additional_permissions") val additionalPermissions: JsonObject?,

    /** Specifies whether the data should be deleted after the consent is done */
    @SerializedName("delete_redundant_data") val deleteRedundantData: Boolean,

    /** ID of the consent being updated (Optional) */
    @SerializedName("existing_consent_id") val existingConsentId: Long?
)
