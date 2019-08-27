/*
 * Copyright 2019 Frollo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package us.frollo.frollosdk.authentication

/**
 * Data source for the authentication class to retrieve access tokens from.
 * This must be implemented by custom authentication implementations.
 */
interface AccessTokenProvider {

    /**
     * Access token to use for authorization if available. If this is not available network requests will not proceed
     */
    val accessToken: AccessToken?
}