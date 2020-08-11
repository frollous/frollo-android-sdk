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

package us.frollo.frollosdk.model.coredata.aggregation.accounts

import com.google.gson.annotations.SerializedName

/**
 * Data representation of AccountFeatureDetail
 */
data class AccountFeatureDetail(

    /** Unique ID of the feature detail */
    @SerializedName("id") val detailId: String,

    /** Name of the feature detail */
    @SerializedName("name") val name: String,

    /** Image URL for the feature detail (optional) */
    @SerializedName("image_url") val imageUrl: String?
)
