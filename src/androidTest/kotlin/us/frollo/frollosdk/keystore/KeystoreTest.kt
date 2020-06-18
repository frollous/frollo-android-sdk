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

package us.frollo.frollosdk.keystore

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KeystoreTest {
    private val keystore = Keystore()

    @Test
    fun testKeyStoreSetup() {
        keystore.setup()
        assertTrue(keystore.isSetup)

        keystore.reset()
    }

    @Test
    fun testKeyStoreEncryptAndDecrypt() {
        keystore.setup()

        val inputStr = "ValidInputString"
        val encryptedStr = keystore.encrypt(inputStr)
        assertNotEquals(inputStr, encryptedStr)

        val decryptedStr = keystore.decrypt(encryptedStr)
        assertEquals(inputStr, decryptedStr)

        keystore.reset()
    }

    @Test
    fun testKeyStoreReset() {
        keystore.setup()

        keystore.reset()
        assertFalse(keystore.isSetup)
    }
}
