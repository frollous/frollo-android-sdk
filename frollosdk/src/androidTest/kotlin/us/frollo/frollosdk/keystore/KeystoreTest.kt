package us.frollo.frollosdk.keystore

import org.junit.Assert.*
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