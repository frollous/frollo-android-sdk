package us.frollo.frollosdk.logging

/**
 * Logging level
 */
enum class LogLevel(internal val score: Int) {
    /** Log all messages including debug statements */
    DEBUG(1),
    /** Log additional information */
    INFO(2),
    /** Log only the most significant errors */
    ERROR(16)
}