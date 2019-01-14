package us.frollo.frollosdk.testutils

import android.content.Context
import androidx.annotation.RawRes
import us.frollo.frollosdk.extensions.toString
import java.io.InputStream
import java.util.*
import java.io.BufferedReader
import java.io.InputStreamReader
import okhttp3.mockwebserver.MockResponse

internal fun randomNumber() = Random().nextInt()

internal fun randomUUID() = UUID.randomUUID().toString()

internal fun today(format: String) = Date().toString(format)

internal fun randomString(length: Int) : String {
    val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    return (1..length)
            .map { kotlin.random.Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("")
}

@Throws(Exception::class)
fun convertStreamToString(inputStream: InputStream): String {
    val reader = BufferedReader(InputStreamReader(inputStream))
    val sb = StringBuilder()
    var line = reader.readLine()
    while (line != null) {
        sb.append(line).append("\n")
        line = reader.readLine()
    }
    reader.close()
    return sb.toString()
}

@Throws(Exception::class)
fun readStringFromJson(context: Context, @RawRes resId: Int): String {
    val stream = context.resources.openRawResource(resId)
    val ret = convertStreamToString(stream)
    //Make sure you close all streams.
    stream.close()
    return ret
}

fun get429Response(): MockResponse {
    return MockResponse().setResponseCode(429)
            .setBody("{\"error\":\"too_many_requests\", \"reason\":\"example reason\"}\r\n")
}

fun wait(seconds: Int) {
    Thread.sleep(seconds * 1000L)
}