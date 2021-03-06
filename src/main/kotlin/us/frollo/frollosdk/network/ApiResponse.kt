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

package us.frollo.frollosdk.network

import retrofit2.Response
import us.frollo.frollosdk.logging.Log
import java.io.IOException
import java.util.regex.Pattern

internal class ApiResponse<T> {
    companion object {
        private const val TAG = "ApiResponse"

        private val LINK_PATTERN = Pattern.compile("<([^>]*)>[\\s]*;[\\s]*rel=\"([a-zA-Z0-9]+)\"")
        private val PAGE_PATTERN = Pattern.compile("\\bpage=(\\d+)")
        private const val NEXT_LINK = "next"
    }

    var code: Int? = null
    var body: T? = null
    var errorMessage: String? = null
    var links: MutableMap<String, String> = mutableMapOf()

    val isSuccessful: Boolean
        get() = code in 200..299

    val nextPage: Int?
        get() {
            val next = links[NEXT_LINK] ?: return null
            val matcher = PAGE_PATTERN.matcher(next)
            if (!matcher.find() || matcher.groupCount() != 1) {
                return null
            }
            return try {
                Integer.parseInt(matcher.group(1))
            } catch (ex: NumberFormatException) {
                Log.d("$TAG.nextPage", String.format("cannot parse next page from %s", next))
                null
            }
        }

    constructor(error: Throwable?) {
        body = null
        errorMessage = error?.message
        links = mutableMapOf()
    }

    constructor(response: Response<T>?) {
        if (response != null) {
            code = response.code()
            if (response.isSuccessful) {
                body = response.body()
                errorMessage = null
            } else {
                var message: String? = try {
                    response.errorBody()?.string()
                } catch (ignored: IOException) {
                    Log.d("$TAG.constructor", "$ignored error while parsing response")
                    null
                }

                if (message == null || message.trim { it <= ' ' }.isEmpty()) {
                    message = response.message()
                }
                errorMessage = message
                body = null
            }
            val linkHeader = response.headers().get("link")
            links = mutableMapOf()

            linkHeader?.let {
                val matcher = LINK_PATTERN.matcher(it)

                while (matcher.find()) {
                    val count = matcher.groupCount()
                    if (count == 2) {
                        links.put(matcher.group(2), matcher.group(1))
                    }
                }
            }
        }
    }
}
