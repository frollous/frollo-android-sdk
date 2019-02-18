package us.frollo.frollosdk.extensions

import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.text.SimpleDateFormat
import java.util.*

internal fun LocalDate.toString(formatPattern: String): String =
        DateTimeFormatter.ofPattern(formatPattern).format(this)

internal fun Date.toString(formatPattern: String): String =
        SimpleDateFormat(formatPattern, Locale.getDefault()).format(this)