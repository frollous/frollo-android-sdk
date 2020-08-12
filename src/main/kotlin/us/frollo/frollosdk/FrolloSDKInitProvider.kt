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

package us.frollo.frollosdk

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import us.frollo.frollosdk.error.FrolloSDKError

class FrolloSDKInitProvider : ContentProvider() {
    override fun onCreate(): Boolean {
        context?.let {
            FrolloSDK.context = it
        } ?: throw FrolloSDKError("Frollo SDK Initialization Failed: Context is null")
        return true
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? { return null }
    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? { return null }
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int { return -1 }
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int { return -1 }
    override fun getType(uri: Uri): String? { return null }
}
