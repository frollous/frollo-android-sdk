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
import androidx.annotation.StringRes
import us.frollo.frollosdk.R

/**
 * Indicates the type of error
 */
enum class NetworkErrorType(
        /** Localized string resource id */
        @StringRes val textResource: Int) {

    /** Connection failure - usually poor connectivity */
    CONNECTION_FAILURE(R.string.FrolloSDK_Error_Network_ConnectionFailure),
    /** Invalid SSL - TLS public key pinning has failed or the certificate provided is invalid. Usually indicates a MITM attack */
    INVALID_SSL(R.string.FrolloSDK_Error_Network_InvalidSSL),
    /** Unknown error */
    UNKNOWN(R.string.FrolloSDK_Error_Network_UnknownError);

    /** Enum to localized message */
    fun toLocalizedString(context: Context?, arg1: String? = null): String? =
            context?.resources?.getString(textResource, arg1)
}