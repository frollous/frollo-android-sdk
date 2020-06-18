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

package us.frollo.frollosdk.network

internal class PublicKey {

    companion object {

        // Production public key, checked 06/11/18
        const val ACTIVE = "sha256/XysGYqMH3Ml0kZoh6zTTaTzR4wYBGgUWfvbxgh4V4QA="

        // Production backup public key, checked 10/01/17 - Not in use
        const val BACKUP = "sha256/UgMkdW5Xlo5dOndGZIdWLSrMu7DD3gwmnyqSOg+gz3I="
    }
}
