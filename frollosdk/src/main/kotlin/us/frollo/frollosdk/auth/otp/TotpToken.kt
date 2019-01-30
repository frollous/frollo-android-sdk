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
package us.frollo.frollosdk.auth.otp

import java.util.Calendar
import java.util.TimeZone


/**
 * TOTP Token
 *
 * Generates an OTP based on the time, for more information see
 * http://tools.ietf.org/html/draft-mraihi-totp-timebased-00
 *
 */
internal class TotpToken(name: String, serial: String, seed: String, private val mTimeStep: Int, otpLength: Int) : HotpToken(name, serial, seed, 0, otpLength) {

    override fun getTimeStep(): Int {
        return mTimeStep
    }

    override fun getTokenType(): Int {
        return TokenDbAdapter.TOKEN_TYPE_TIME
    }

    override fun generateOtp(): String {

        //calculate the moving counter using the time
        return generateOtp(Calendar.getInstance(TimeZone.getTimeZone("GMT")))
    }

    fun generateOtp(currentTime: Calendar): String {
        val time = currentTime.timeInMillis / 1000
        super.setEventCount(time / mTimeStep)

        return super.generateOtp()
    }


}
