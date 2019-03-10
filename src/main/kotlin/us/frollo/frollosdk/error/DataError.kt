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

package us.frollo.frollosdk.error

import com.google.gson.annotations.SerializedName

/**
 * Error caused by an issue with data or data storage
 */
data class DataError(
        /** Data error type */
        @SerializedName("type") val type: DataErrorType,
        /** More detailed sub type of the error */
        @SerializedName("sub_type") val subType: DataErrorSubType
) : FrolloSDKError() {

    /** Localized description */
    override val localizedDescription : String?
        get() = if (subType.type == type) subType.toLocalizedString(context)
                else type.toLocalizedString(context)

    /** Debug description */
    override val debugDescription: String?
        get() = "DataError: ${ type.name }.${ subType.name }: $localizedDescription"
}