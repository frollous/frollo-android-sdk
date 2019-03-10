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

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*
import us.frollo.frollosdk.BuildConfig
import us.frollo.frollosdk.preferences.Preferences

class VersionTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
    private lateinit var preferences: Preferences

    @Before
    fun setUp() {
        preferences = Preferences(context)
    }

    @After
    fun tearDown() {
        preferences.resetAll()
    }

    @Test
    fun testMigrationNeededFalse() {
        preferences.sdkVersion = BuildConfig.VERSION_NAME
        val version = Version(preferences)
        assertFalse(version.migrationNeeded())
    }

    @Test
    fun testMigrationNeededTrue() {
        preferences.sdkVersion = "0.1.2"
        val version = Version(preferences)
        assertTrue(version.migrationNeeded())
    }

    @Test
    fun testMigrationNeededFirstInstall() {
        assertNull(preferences.sdkVersion)
        assertTrue(preferences.sdkVersionHistory.isEmpty())
        val version = Version(preferences)
        assertFalse(version.migrationNeeded())
        assertEquals(BuildConfig.VERSION_NAME, preferences.sdkVersion)
        assertEquals(BuildConfig.VERSION_NAME, preferences.sdkVersionHistory[0])
    }

    @Test
    fun testMigrateVersion() {
        preferences.sdkVersion = "0.1.2"
        preferences.sdkVersionHistory = mutableListOf("0.1.1", "0.1.2")
        val version = Version(preferences)
        version.migrateVersion()
        assertEquals(BuildConfig.VERSION_NAME, preferences.sdkVersion)
        assertEquals(BuildConfig.VERSION_NAME, preferences.sdkVersionHistory[2])
    }
}