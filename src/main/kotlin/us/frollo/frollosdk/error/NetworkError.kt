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

import java.io.IOException
import java.security.GeneralSecurityException
import javax.net.ssl.SSLException

/**
 * Error occuring at the network layer
 */
class NetworkError(private val t: Throwable? = null) : FrolloSDKError(t?.message) {

    /** Type of error for common scenarios */
    val type: NetworkErrorType
        get() {
            return if (t is SSLException || t is GeneralSecurityException) {
                NetworkErrorType.INVALID_SSL
            } else if (t is IOException) {
                NetworkErrorType.CONNECTION_FAILURE
            } else {
                NetworkErrorType.UNKNOWN
            }
        }

    /** Localized description */
    override val localizedDescription: String?
        get() {
            var msg = type.toLocalizedString(context)
            t?.let { msg = msg.plus("\n\n${it.message}") }
            return msg
        }

    /** Debug description */
    override val debugDescription: String?
        get() = "NetworkError: ${ type.name }: $localizedDescription"
}
