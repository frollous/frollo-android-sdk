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

import android.content.Context
import us.frollo.frollosdk.FrolloSDK
import us.frollo.frollosdk.R

/**
 * Conforms to localizable error and debug description
 */
open class FrolloSDKError(errorMessage: String? = null) : Error(errorMessage) {

    internal val context: Context? = FrolloSDK.context

    /** Localized description */
    open val localizedDescription: String? =
        errorMessage ?: context?.resources?.getString(R.string.FrolloSDK_Error_Generic_UnknownError)

    /** Debug description */
    open val debugDescription: String? =
        errorMessage ?: context?.resources?.getString(R.string.FrolloSDK_Error_Generic_UnknownError)
}
