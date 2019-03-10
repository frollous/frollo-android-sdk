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

package us.frollo.frollosdk.testutils

import android.content.Context
import androidx.annotation.RawRes
import us.frollo.frollosdk.extensions.toString
import java.io.InputStream
import java.util.*
import java.io.BufferedReader
import java.io.InputStreamReader
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest

internal fun randomNumber(range: IntRange? = null)
        = if (range == null) Random().nextInt() else range.random()

internal fun randomUUID() = UUID.randomUUID().toString()

internal fun today(format: String) = Date().toString(format)

internal fun randomString(length: Int) : String {
    val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    return (1..length)
            .map { kotlin.random.Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("")
}

internal fun randomBoolean() = Random().nextBoolean()

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

val RecordedRequest.trimmedPath: String
    get() = path.replace(Regex("^/+"), "")