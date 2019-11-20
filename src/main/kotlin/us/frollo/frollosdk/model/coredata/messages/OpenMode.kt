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

/** Indicates the open mode of the link and how it should be opened */
enum class OpenMode {
    /** Open the link internally using native webview and no user controls */
    @SerializedName("internal") INTERNAL,
    /** Open the link internally using native webview with navigation controls */
    @SerializedName("internal_navigation") INTERNAL_NAVIGATION,
    /** Open the link internally using the secure web view - ChromeTabs */
    @SerializedName("internal_secure") INTERNAL_SECURE,
    /** Open the link using the native browser on the phone (or app if deeplink) */
    @SerializedName("external") EXTERNAL;

    /** Enum to serialized string */
    // This override MUST be used for this enum to work with Retrofit @Path or @Query parameters
    override fun toString(): String =
    // Try to get the annotation value if available instead of using plain .toString()
    // Fallback to super.toString() in case annotation is not present/available
            serializedName() ?: super.toString()
}