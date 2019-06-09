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

package us.frollo.frollosdk.network.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import us.frollo.frollosdk.model.oauth.OAuthTokenRequest
import us.frollo.frollosdk.model.oauth.OAuthTokenResponse
import us.frollo.frollosdk.model.oauth.OAuthTokenRevokeRequest

internal interface TokenAPI {
    companion object {
        const val URL_TOKEN = "." // Use . to indicate the endpoint url is same as base url
    }

    @POST(URL_TOKEN)
    fun refreshTokens(@Body request: OAuthTokenRequest): Call<OAuthTokenResponse>

    @POST(URL_TOKEN)
    fun revokeToken(@Body request: OAuthTokenRevokeRequest): Call<Void>
}