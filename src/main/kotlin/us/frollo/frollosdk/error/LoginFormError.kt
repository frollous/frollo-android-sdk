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

/**
 * Error occuring when using the aggregation provider login forms
 */
class LoginFormError(
    /** Login form error type */
    val type: LoginFormErrorType,
    /** Affected field name */
    val fieldName: String
) : FrolloSDKError() {

    /** Additional error information */
    var additionalError: String? = null

    /** Localized description */
    override val localizedDescription: String?
        get() {
            var description = type.toLocalizedString(context, fieldName)
            additionalError?.let { description = description.plus(" $it") }
            return description
        }

    /** Debug description */
    override val debugDescription: String?
        get() = "LoginFormError: ${ type.name }: $localizedDescription"
}
