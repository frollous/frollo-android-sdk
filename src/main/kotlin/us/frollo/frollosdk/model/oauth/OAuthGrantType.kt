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
import us.frollo.frollosdk.extensions.serializedName

/**
 * OAuth Grant Type
 */
enum class OAuthGrantType {

    /** OAuth Grant Type based on Authorization Code */
    @SerializedName("authorization_code") AUTHORIZATION_CODE,

    /** OAuth Grant Type based on Password */
    @SerializedName("password") PASSWORD,

    /** OAuth Grant Type based on Refresh Token */
    @SerializedName("refresh_token") REFRESH_TOKEN,

    /** OAuth Grant Type based on Realm Password */
    @SerializedName("http://auth0.com/oauth/grant-type/password-realm") REALM_PASSWORD;

    /** Enum to serialized string */
    // This override MUST be used for this enum to work with Retrofit @Path or @Query parameters
    override fun toString(): String =
        // Try to get the annotation value if available instead of using plain .toString()
        // Fallback to super.toString() in case annotation is not present/available
        serializedName() ?: super.toString()
}
