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

/**
 * All spec-defined values for the OAuth2 / OpenID Connect 1.0 `scope` parameter.
 *
 * @see "OpenID Connect Core 1.0, Section 5.4
 * <https://openid.net/specs/openid-connect-core-1_0.html#rfc.section.5.4>"
 */
class OAuth2Scope {

    companion object {
        /**
         * A scope for the authenticated user's mailing address.
         *
         * @see "OpenID Connect Core 1.0, Section 5.4
         * <https:></https:>//openid.net/specs/openid-connect-core-1_0.html.rfc.section.5.4>"
         */
        const val ADDRESS = "address"

        /**
         * A scope for the authenticated user's email address.
         *
         * @see "OpenID Connect Core 1.0, Section 5.4
         * <https:></https:>//openid.net/specs/openid-connect-core-1_0.html.rfc.section.5.4>"
         */
        const val EMAIL = "email"

        /**
         * A scope for requesting an OAuth 2.0 refresh token to be issued, that can be used to
         * obtain an Access Token that grants access to the End-User's UserInfo Endpoint even
         * when the End-User is not present (not logged in).
         *
         * @see "OpenID Connect Core 1.0, Section 11
         * <https:></https:>//openid.net/specs/openid-connect-core-1_0.html.rfc.section.11>"
         */
        const val OFFLINE_ACCESS = "offline_access"

        /**
         * A scope for OpenID based authorization.
         *
         * @see "OpenID Connect Core 1.0, Section 3.1.2.1
         * <https:></https:>//openid.net/specs/openid-connect-core-1_0.html.rfc.section.3.1.2.1>"
         */
        const val OPENID = "openid"

        /**
         * A scope for the authenticated user's phone number.
         *
         * @see "OpenID Connect Core 1.0, Section 5.4
         * <https:></https:>//openid.net/specs/openid-connect-core-1_0.html.rfc.section.5.4>"
         */
        const val PHONE = "phone"

        /**
         * A scope for the authenticated user's basic profile information.
         *
         * @see "OpenID Connect Core 1.0, Section 5.4
         * <https:></https:>//openid.net/specs/openid-connect-core-1_0.html.rfc.section.5.4>"
         */
        const val PROFILE = "profile"
    }
}
