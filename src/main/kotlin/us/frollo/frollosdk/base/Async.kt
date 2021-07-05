package us.frollo.frollosdk.base

import android.os.Handler
import android.os.Looper
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

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
