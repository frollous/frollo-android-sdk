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

package us.frollo.frollosdk.model.coredata.user

import com.google.gson.annotations.SerializedName

/**
 * User Attribution
 */
data class Attribution(
    /** Attribution network of the user (optional) */
    @SerializedName("network") var network: String? = null,
    /** Attribution campaign of the user (optional) */
    @SerializedName("campaign") var campaign: String? = null,
    /** Attribution creative of the user (optional) */
    @SerializedName("creative") var creative: String? = null,
    /** Attribution ad group of the user (optional) */
    @SerializedName("ad_group") var adGroup: String? = null
)