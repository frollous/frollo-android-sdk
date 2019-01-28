package us.frollo.frollosdk.logging

import org.junit.Test

import org.junit.Assert.*

class ConsoleLoggerTest {

    @Test
    fun testDebugMessage() {
        val logger = ConsoleLogger()
        logger.writeMessage("Test log message", LogLevel.DEBUG)
    }

    @Test
    fun testInfoMessage() {
        val logger = ConsoleLogger()
        logger.writeMessage("Test log message", LogLevel.INFO)
    }

    @Test
    fun testErrorMessage() {
        val logger = ConsoleLogger()
        logger.writeMessage("Test log message", LogLevel.ERROR)
    }
}