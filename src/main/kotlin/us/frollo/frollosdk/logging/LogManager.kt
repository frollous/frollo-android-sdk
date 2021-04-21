package us.frollo.frollosdk.logging

/** Manages logging error and info to the host */
class LogManager {

    /** Upload debug logs */
    fun debug(tag: String, message: String?) {
        Log.d(tag, message)
    }

    /** Upload error logs */
    fun error(tag: String, message: String?) {
        Log.e(tag, message)
    }

    /** Upload info logs */
    fun info(tag: String, message: String?) {
        Log.e(tag, message)
    }
}
