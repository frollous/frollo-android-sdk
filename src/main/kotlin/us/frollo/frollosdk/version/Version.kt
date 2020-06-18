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

package us.frollo.frollosdk.version

import us.frollo.frollosdk.BuildConfig
import us.frollo.frollosdk.preferences.Preferences

internal class Version(private val pref: Preferences) {

    private var currentVersion = BuildConfig.VERSION_NAME
    private var previousVersion: String? = null
    private var versionHistory: MutableSet<String>

    init {
        previousVersion = pref.sdkVersion
        versionHistory = pref.sdkVersionHistory.toMutableSet()
    }

    fun migrationNeeded(): Boolean {
        previousVersion?.let { prev ->
            if (prev != currentVersion) {
                return true
            }
        } ?: run {
            // First install
            initialiseVersion()
        }
        return false
    }

    fun migrateVersion() {
        if (previousVersion == null) return

        // Stubbed for future. Replace null check with let and iterate through versions

        updateVersion()
    }

    private fun initialiseVersion() {
        updateVersion()
    }

    private fun updateVersion() {
        previousVersion = currentVersion
        versionHistory.add(currentVersion)

        pref.sdkVersion = currentVersion
        pref.sdkVersionHistory = versionHistory.toMutableList()
    }
}
