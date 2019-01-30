package us.frollo.frollosdk.logging

import us.frollo.frollosdk.data.remote.NetworkService
import us.frollo.frollosdk.data.remote.api.DeviceAPI
import us.frollo.frollosdk.extensions.enqueue
import us.frollo.frollosdk.model.api.device.LogRequest

internal class NetworkLogger(private val network: NetworkService?) : Logger() {

    private val deviceAPI : DeviceAPI? = network?.create(DeviceAPI::class.java)

    override fun writeMessage(message: String, logLevel: LogLevel) {
        val hasTokens = network?.hasTokens() ?: false
        if (!hasTokens) {
            return
        }

        deviceAPI?.createLog(LogRequest(message = message, score = logLevel.score))?.enqueue { _, _ ->  }
    }
}