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

package us.frollo.frollosdk.core

internal fun testSDKConfig(
    clientId: String? = null,
    serverUrl: String? = null,
    tokenUrl: String? = null,
    authorizationUrl: String? = null,
    redirectUrl: String? = null,
    revokeTokenURL: String? = null
) =
        FrolloSDKConfiguration(
                clientId = clientId ?: "abc123",
                redirectUrl = redirectUrl ?: "app://redirect",
                authorizationUrl = authorizationUrl ?: "https://id.example.com/oauth/authorize/",
                tokenUrl = tokenUrl ?: "https://id.example.com/oauth/token/",
                serverUrl = serverUrl ?: "https://api.example.com/",
                revokeTokenURL = revokeTokenURL ?: "https://id.example.com/oauth/revoke")
