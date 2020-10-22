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

package us.frollo.frollosdk.model.api.aggregation.providers

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import us.frollo.frollosdk.model.coredata.aggregation.cdr.CDRPermission
import us.frollo.frollosdk.model.coredata.aggregation.providers.AggregatorType
import us.frollo.frollosdk.model.coredata.aggregation.providers.ProviderContainerName
import us.frollo.frollosdk.model.coredata.aggregation.providers.ProviderStatus

/**
 * This is just an minimized model representing the response of
 * fetchAllProviders() from host.
 *
 * We are using this class as partial entity for Provider Entity.
 */
internal data class ProvidersResponse(
    @PrimaryKey @ColumnInfo(name = "provider_id") val providerId: Long,
    @ColumnInfo(name = "provider_name") val providerName: String,
    @ColumnInfo(name = "small_logo_url") val smallLogoUrl: String,
    @ColumnInfo(name = "small_logo_revision") val smallLogoRevision: Int,
    @ColumnInfo(name = "provider_status") val providerStatus: ProviderStatus,
    @ColumnInfo(name = "popular") val popular: Boolean,
    @ColumnInfo(name = "container_names") val containerNames: List<ProviderContainerName>,
    @ColumnInfo(name = "login_url") val loginUrl: String?,
    @ColumnInfo(name = "large_logo_url") val largeLogoUrl: String?,
    @ColumnInfo(name = "large_logo_revision") val largeLogoRevision: Int?,
    @ColumnInfo(name = "aggregator_type") val aggregatorType: AggregatorType,
    @ColumnInfo(name = "permissions") val permissions: List<CDRPermission>?,
    @ColumnInfo(name = "products_available") val productsAvailable: Boolean?
)
