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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.Month
import java.util.Calendar

class FormattingExtensionTest {

    @Test
    fun testLocalDateToString() {
        val date = LocalDate.of(2019, Month.JANUARY, 2)
        val str = date.toString("dd-MM-yyyy")
        assertEquals("02-01-2019", str)
    }

    @Test
    fun testDateToString() {
        val cal = Calendar.getInstance()
        cal.set(2019, Calendar.JANUARY, 2)
        val date = cal.time
        val str = date.toString("dd-MM-yyyy")
        assertEquals("02-01-2019", str)
    }

    @Test
    fun testDateStringIsValidFormat() {
        var dateStr = "2019-02-01"
        assertTrue(dateStr.isValidFormat("yyyy-MM-dd"))

        dateStr = "2019-02"
        assertTrue(dateStr.isValidFormat("yyyy-MM"))

        dateStr = "01-02-2019"
        assertFalse(dateStr.isValidFormat("yyyy-MM-dd"))

        dateStr = "2019-02"
        assertFalse(dateStr.isValidFormat("yyyy-MM-dd"))

        dateStr = "02-2019"
        assertFalse(dateStr.isValidFormat("yyyy-MM-dd"))
    }

    @Test
    fun testChangeDateFormat() {
        var oldDateStr = "2019-02-01"
        var newDateStr = oldDateStr.changeDateFormat("yyyy-MM-dd", "yyyy-MM")
        assertEquals("2019-02", newDateStr)

        oldDateStr = "2019-02"
        newDateStr = oldDateStr.changeDateFormat("yyyy-MM", "yyyy-MM-dd")
        assertEquals("2019-02-01", newDateStr)
    }

    @Test
    fun testDateFormatDailyToWeekly() {
        var oldDateStr = "2019-02-28"
        var newDateStr = oldDateStr.dailyToWeekly()
        assertEquals("2019-02-4", newDateStr)

        oldDateStr = "2019-02-08"
        newDateStr = oldDateStr.dailyToWeekly()
        assertEquals("2019-02-2", newDateStr)
    }

    @Test
    fun testGetWeekInMonth() {
        for (i in 1..7) {
            assertEquals(1, getWeekInMonth(i))
        }
        for (i in 8..14) {
            assertEquals(2, getWeekInMonth(i))
        }
        for (i in 15..21) {
            assertEquals(3, getWeekInMonth(i))
        }
        for (i in 22..28) {
            assertEquals(4, getWeekInMonth(i))
        }
        for (i in 29..31) {
            assertEquals(5, getWeekInMonth(i))
        }
    }
}
