package us.frollo.frollosdk.extensions

import org.junit.Assert
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.Month

class FormattingExtensionTest {

    @Test
    fun testLocalDateToString() {
        val date = LocalDate.of(2019, Month.JANUARY, 2)
        val str = date.toString("dd-MM-yyyy")
        Assert.assertEquals("02-01-2019", str)
    }
}