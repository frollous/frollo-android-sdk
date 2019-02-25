package us.frollo.frollosdk.authentication.otp

internal class OTP {

    companion object {

        fun generateOTP(packageName: String): String {
            val uniqueStr = packageName.plus(packageName)
            val token = TotpToken("test", "test", asciiToHex(uniqueStr), 30, 8)
            return token.generateOtp()
        }

        private fun asciiToHex(asciiValue: String): String {
            val chars = asciiValue.toCharArray()
            val hex = StringBuffer()
            for (i in chars.indices)
                hex.append(Integer.toHexString(chars[i].toInt()))
            return hex.toString()
        }
    }
}