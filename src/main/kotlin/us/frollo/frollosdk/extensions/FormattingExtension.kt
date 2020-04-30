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

package us.frollo.frollosdk.extensions

import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.DateTimeFormatterBuilder
import org.threeten.bp.format.DateTimeParseException
import org.threeten.bp.temporal.ChronoField
import java.lang.Exception
import java.lang.RuntimeException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal fun LocalDate.toString(formatPattern: String): String =
        DateTimeFormatter.ofPattern(formatPattern).format(this)

internal fun Date.toString(formatPattern: String): String =
        SimpleDateFormat(formatPattern, Locale.getDefault()).format(this)

internal fun String.isValidFormat(formatPattern: String): Boolean {
    val formatter = DateTimeFormatter.ofPattern(formatPattern)
    try {
        formatter.parse(this) // if not valid, it will throw DateTimeParseException
    } catch (e: DateTimeParseException) {
        return false
    } catch (e: RuntimeException) {
        return false
    }
    return true
}

internal fun String.changeDateFormat(from: String, to: String): String {
    var dateStr = ""

    try {
        val sourceFormatter = DateTimeFormatterBuilder()
                .appendPattern(from)
                .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
                .toFormatter()
        val date = LocalDate.parse(this, sourceFormatter)
        dateStr = date.toString(to)
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return dateStr
}

internal fun String.dailyToWeekly(): String {
    var dateStr = ""

    try {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val dailyDate = LocalDate.parse(this, formatter)
        val monthlyDateStr = changeDateFormat("yyyy-MM-dd", "yyyy-MM")
        val weekInMonth = getWeekInMonth(dailyDate.dayOfMonth)
        dateStr = monthlyDateStr.plus("-$weekInMonth")
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return dateStr
}

internal fun getWeekInMonth(dayOfMonth: Int): Int {
    return when (dayOfMonth) {
        in 1..7 -> 1
        in 8..14 -> 2
        in 15..21 -> 3
        in 22..28 -> 4
        else -> 5
    }
}

fun String.toLocalDate(pattern: String): LocalDate {
    val formatter = DateTimeFormatterBuilder()
            .appendPattern(pattern)
            .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
            .toFormatter()
    return LocalDate.parse(this, formatter)
}