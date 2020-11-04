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

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.model.coredata.cdr.ConsentUpdateForm

internal data class ConsentUpdateRequest(

    /** The new status for the consent (Optional)*/
    @SerializedName("status") val status: ConsentUpdateForm.ConsentUpdateStatus?,

    /** The new value for the delete redundant data (Optional) */
    @SerializedName("delete_redundant_data") val deleteRedundantData: Boolean?,

    /** The new value for duration (in seconds) for the consent (Optional) */
    @SerializedName("sharing_duration") val sharingDuration: Long?
)
