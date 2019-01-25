package us.frollo.frollosdk.logging

internal abstract class Logger {

    abstract fun writeMessage(message: String, level: LogLevel)
}