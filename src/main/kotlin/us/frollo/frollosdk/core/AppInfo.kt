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

package us.frollo.frollosdk.core

import android.content.Context
import android.content.pm.PackageInfo
import java.lang.Exception

/**
 * @suppress
 */
class AppInfo(private val context: Context) {

    internal val versionNumber: String
        get() {
            return try {
                packageInfo.versionName
            } catch (e: Exception) {
                "1"
            }
        }

    internal val versionCode: Int
        get() {
            return try {
                packageInfo.versionCode
            } catch (e: Exception) {
                1
            }
        }

    private val packageInfo: PackageInfo
        get() {
            val manager = context.packageManager
            return manager.getPackageInfo(context.packageName, 0)
        }
}
