package us.frollo.frollosdk.testutils

import org.threeten.bp.LocalDate
import us.frollo.frollosdk.extensions.toString
import java.util.*

internal fun randomNumber() = Random().nextInt()

internal fun randomUUID() = UUID.randomUUID().toString()

internal fun today(format: String) = LocalDate.now().toString(format)

internal fun randomString(length: Int) : String {
    val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    return (1..length)
            .map { kotlin.random.Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("")
}