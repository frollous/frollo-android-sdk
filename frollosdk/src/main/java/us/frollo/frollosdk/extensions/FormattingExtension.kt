package us.frollo.frollosdk.extensions

import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

fun LocalDate.toString(formatPattern: String): String =
    DateTimeFormatter.ofPattern(formatPattern).format(this)