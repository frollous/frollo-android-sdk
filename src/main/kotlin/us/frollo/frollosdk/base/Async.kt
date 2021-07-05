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

package us.frollo.frollosdk.base

import android.os.Handler
import android.os.Looper
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/*
* Inspired by https://github.com/Kotlin/anko/blob/master/anko/library/static/commons/src/main/java/Async.kt
* as we wanted to remove anko because its fetched from jcenter
* */

fun uiThread(f: () -> Unit): Boolean {
    if (Looper.getMainLooper() === Looper.myLooper()) {
        f.invoke()
    } else {
        ContextHelper.handler.post { f.invoke() }
    }
    return true
}

/**
 * Execute [task] asynchronously.
 *
 * @param exceptionHandler optional exception handler.
 *  If defined, any exceptions thrown inside [task] will be passed to it. If not, exceptions will be ignored.
 * @param task the code to execute asynchronously.
 */
fun doAsync(
    exceptionHandler: ((Throwable) -> Unit)? = crashLogger,
    task: () -> Unit
) {
    return BackgroundExecutor.submit {
        return@submit try {
            task()
        } catch (thr: Throwable) {
            val result = exceptionHandler?.invoke(thr)
            if (result != null) {
                result
            } else {
                Unit
            }
        }
    }
}

private val crashLogger = { throwable: Throwable -> throwable.printStackTrace() }

internal object BackgroundExecutor {
    var executor: ExecutorService =
        Executors.newScheduledThreadPool(2 * Runtime.getRuntime().availableProcessors())

    fun submit(task: () -> Unit): Unit = executor.execute(task)
}

private object ContextHelper {
    val handler = Handler(Looper.getMainLooper())
}
