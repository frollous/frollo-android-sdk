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

package us.frollo.frollosdk.model.oauth

import com.google.gson.annotations.SerializedName

internal data class OAuthTokenRequest(
    @SerializedName("grant_type") val grantType: OAuthGrantType,
    @SerializedName("client_id") val clientId: String,
    @SerializedName("domain") val domain: String,
    @SerializedName("code") val code: String? = null,
    @SerializedName("code_verifier") val codeVerifier: String? = null,
    @SerializedName("state") val state: String? = null,
    @SerializedName("redirect_uri") val redirectUrl: String? = null,
    @SerializedName("refresh_token") val refreshToken: String? = null,
    @SerializedName("frollo_legacy_token") val legacyToken: String? = null,
    @SerializedName("username") val username: String? = null,
    @SerializedName("password") val password: String? = null,
    @SerializedName("audience") val audience: String? = null,
    @SerializedName("scope") val scope: String? = null,
    @SerializedName("realm") val realm: String? = null,
) {
    val valid: Boolean
        get() = when (grantType) {
            OAuthGrantType.AUTHORIZATION_CODE -> code != null && redirectUrl != null
            OAuthGrantType.PASSWORD,
            OAuthGrantType.REALM_PASSWORD -> (password != null && username != null) || legacyToken != null
            OAuthGrantType.REFRESH_TOKEN -> refreshToken != null
        }
}
