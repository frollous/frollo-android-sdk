/*
 * Copyright Mark McAvoy - www.bitethebullet.co.uk 2009
 *
 * This file is part of Android Token.
 *
 * Android Token is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Android Token is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Android Token.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package us.frollo.frollosdk.authentication.otp

import java.lang.reflect.UndeclaredThrowableException
import java.security.GeneralSecurityException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

internal abstract class HotpToken(
        private var mName: String,
        private var mSerial: String,
        private var mSeed: String,
        private var mEventCount: Long,
        private var mOtpLength: Int): IToken {

    private var mId: Long = 0

    private val DIGITS_POWER = intArrayOf(1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000) // 0 1  2   3    4     5      6       7        8

    override fun getTimeStep(): Int {
        return 0
    }

    override fun getId() = mId
    override fun setId(id: Long) { mId = id }

    override fun getTokenType() = TokenDbAdapter.TOKEN_TYPE_EVENT

    override fun getName() = mName
    fun setName(name: String) { mName = name }

    override fun getSerialNumber() = mSerial
    fun setSerialNumber(serial: String) { mSerial = serial }

    fun getSeed() = mSeed
    fun setSeed(seed: String) { mSeed = seed }

    fun getEventCount() = mEventCount
    fun setEventCount(eventCount: Long) { mEventCount = eventCount }

    fun getOtpLength(): Int { return mOtpLength }
    fun setOtpLength(otpLength: Int) { mOtpLength = otpLength }

    override fun generateOtp(): String {
        val counter = ByteArray(8)
        var movingFactor = mEventCount

        for (i in counter.indices.reversed()) {
            counter[i] = (movingFactor and 0xff).toByte()
            movingFactor = movingFactor shr 8
        }

        val hash = hmacSha(stringToHex(mSeed), counter)
        val offset = (hash[hash.size - 1]).toInt() and (0xf).toInt()

        val otpBinary = (hash[offset].toInt() and (0x7f).toInt() shl 24
                or (hash[offset + 1].toInt() and (0xff).toInt() shl 16)
                or (hash[offset + 2].toInt() and (0xff).toInt() shl 8)
                or (hash[offset + 3].toInt() and (0xff).toInt()))

        val otp = otpBinary % DIGITS_POWER[mOtpLength]
        var result = Integer.toString(otp)


        while (result.length < mOtpLength) {
            result = "0$result"
        }

        return result
    }

    fun stringToHex(hexInputString: String): ByteArray {

        val bts = ByteArray(hexInputString.length / 2)

        for (i in bts.indices) {
            bts[i] = Integer.parseInt(hexInputString.substring(2 * i, 2 * i + 2), 16).toByte()
        }

        return bts
    }

    private fun hmacSha(seed: ByteArray, counter: ByteArray): ByteArray {

        try {
            var hmacSha1: Mac

            try {
                hmacSha1 = Mac.getInstance("HmacSHA256")
            } catch (ex: NoSuchAlgorithmException) {
                hmacSha1 = Mac.getInstance("HMAC-SHA-256")
            }

            val macKey = SecretKeySpec(seed, "RAW")
            hmacSha1.init(macKey)

            return hmacSha1.doFinal(counter)

        } catch (ex: GeneralSecurityException) {
            throw UndeclaredThrowableException(ex)
        }

    }

    /**
     * Generates a new seed value for a token
     * the returned string will contain a randomly generated
     * hex value
     * @param length - defines the length of the new seed this should be either 128 or 160
     * @return
     */
    fun generateNewSeed(length: Int): String? {

        var salt = ""
        val ticks = Calendar.getInstance(TimeZone.getTimeZone("GMT")).timeInMillis
        salt = salt + ticks

        val byteToHash = salt.toByteArray()

        val md: MessageDigest

        try {
            if (length == 128) {
                //128 long
                md = MessageDigest.getInstance("MD5")
            } else {
                //160 long
                md = MessageDigest.getInstance("SHA1")
            }

            md.reset()
            md.update(byteToHash)

            val digest = md.digest()

            //convert to hex string

            return byteArrayToHexString(digest)

        } catch (ex: NoSuchAlgorithmException) {
            return null
        }

    }


    fun byteArrayToHexString(digest: ByteArray): String {

        val buffer = StringBuffer()

        for (i in digest.indices) {
            val hex = Integer.toHexString((0xff).toInt() and digest[i].toInt())

            if (hex.length == 1)
                buffer.append("0")

            buffer.append(hex)

        }

        return buffer.toString()
    }
}