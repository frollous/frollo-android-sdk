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

package us.frollo.frollosdk.model.coredata.cdr

import com.google.gson.JsonObject

/**
 * Consent form that can be submitted to create a consent with a provider
 */
data class ConsentCreateForm(

    /** ID of the provider to submit consent for */
    var providerId: Long,

    /** The duration (in seconds) for the consent */
    var sharingDuration: Long,

    /** List of permission IDs requested for the consent. Refer to [CDRPermission.permissionId] */
    var permissions: List<String>,

    /** Additional permissions (meta-data map of String:Boolean) that can be set (Optional) */
    var additionalPermissions: JsonObject? = null,

    /** ID of the consent being updated (Optional) */
    var existingConsentId: Long? = null
)
