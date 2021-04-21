package us.frollo.frollosdk.logging

class LogManager {

    fun debug(tag: String, message: String?) {
        Log.d(tag, message)
    }

    fun error(tag: String, message: String?) {
        Log.e(tag, message)
    }

    fun info(tag: String, message: String?) {
        Log.e(tag, message)
    }
}
