package us.frollo.frollosdk.logging

import android.util.Log

internal class ConsoleLogger : Logger() {

    override fun writeMessage(message: String, logLevel: LogLevel) {
        when(logLevel) {
            LogLevel.DEBUG -> Log.d("FrolloSDKLogger", message)
            LogLevel.INFO -> Log.i("FrolloSDKLogger", message)
            LogLevel.ERROR -> Log.e("FrolloSDKLogger", message)
        }
    }
}