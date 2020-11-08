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

package us.frollo.frollosdk.model.coredata.cdr

import com.google.gson.annotations.SerializedName

/** Represents the sharing duration of a consent */
data class SharingDuration(

    /** The duration (in seconds) for the consent */
    @SerializedName("duration") val duration: Long,

    /** The display text of the sharing duration */
    @SerializedName("description") val description: String,

    /** The image URL for the sharing duration image */
    @SerializedName("image_url") val imageUrl: String
)
