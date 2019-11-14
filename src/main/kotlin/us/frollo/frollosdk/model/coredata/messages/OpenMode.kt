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

package us.frollo.frollosdk.model.coredata.messages

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.extensions.serializedName

/** Indicates the content type of the message and how it should be rendered */
enum class OpenMode {
    /** Open in a webview inside the app */
    @SerializedName("internal") INTERNAL,
    /** Open in a webview inside the app & show back,forward and refresh buttons */
    @SerializedName("internal_navigation") INTERNAL_NAVIGATION,
    /** Open in a webview inside the app & create a secure login from the app */
    @SerializedName("internal_secure") INTERNAL_SECURE,
    /** Open in a webview in an external browser */
    @SerializedName("external") EXTERNAL;

    /** Enum to serialized string */
    // This override MUST be used for this enum to work with Retrofit @Path or @Query parameters
    override fun toString(): String =
    // Try to get the annotation value if available instead of using plain .toString()
    // Fallback to super.toString() in case annotation is not present/available
            serializedName() ?: super.toString()
}