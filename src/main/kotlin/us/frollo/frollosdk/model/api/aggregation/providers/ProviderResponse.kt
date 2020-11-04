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

package us.frollo.frollosdk.model.api.aggregation.providers

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.model.coredata.aggregation.providers.AggregatorType
import us.frollo.frollosdk.model.coredata.aggregation.providers.ProviderAuthType
import us.frollo.frollosdk.model.coredata.aggregation.providers.ProviderEncryption
import us.frollo.frollosdk.model.coredata.aggregation.providers.ProviderLoginForm
import us.frollo.frollosdk.model.coredata.aggregation.providers.ProviderMFAType
import us.frollo.frollosdk.model.coredata.aggregation.providers.ProviderStatus
import us.frollo.frollosdk.model.coredata.cdr.CDRPermission

internal data class ProviderResponse(

    // Fields that are also returned via GET Providers List API response
    @SerializedName("id") val providerId: Long,
    @SerializedName("name") val providerName: String,
    @SerializedName("small_logo_url") val smallLogoUrl: String?,
    @SerializedName("small_logo_revision") val smallLogoRevision: Int?,
    @SerializedName("status") val providerStatus: ProviderStatus,
    @SerializedName("popular") val popular: Boolean,
    @SerializedName("container_names") val containerNames: List<String>,
    @SerializedName("login_url") val loginUrl: String?,
    @SerializedName("large_logo_url") val largeLogoUrl: String?,
    @SerializedName("large_logo_revision") val largeLogoRevision: Int?,
    @SerializedName("aggregator_type") val aggregatorType: AggregatorType,
    @SerializedName("permissions") val permissions: List<CDRPermission>?,
    @SerializedName("products_available") val productsAvailable: Boolean?,

    // Fields that are specific to GET Provider API response
    @SerializedName("base_url") val baseUrl: String?,
    @SerializedName("forget_password_url") val forgetPasswordUrl: String?,
    @SerializedName("o_auth_site") val oAuthSite: Boolean?,
    @SerializedName("auth_type") val authType: ProviderAuthType?,
    @SerializedName("mfa_type") val mfaType: ProviderMFAType?,
    @SerializedName("help_message") val helpMessage: String?,
    @SerializedName("login_help_message") val loginHelpMessage: String?,
    @SerializedName("login_form") val loginForm: ProviderLoginForm?,
    @SerializedName("encryption") val encryption: ProviderEncryption?
)
