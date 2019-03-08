package us.frollo.frollosdk.logging

import us.frollo.frollosdk.network.NetworkService
import us.frollo.frollosdk.network.api.DeviceAPI
import us.frollo.frollosdk.extensions.enqueue
import us.frollo.frollosdk.model.api.device.LogRequest

internal class NetworkLogger(
        private val network: NetworkService?,
        private val deviceId: String?,
        private val deviceType: String?,
        private val deviceName: String?) : Logger() {

    private val deviceAPI : DeviceAPI? = network?.create(DeviceAPI::class.java)

    override fun writeMessage(message: String, logLevel: LogLevel) {
        val hasTokens = network?.hasTokens() ?: false
        if (!hasTokens || deviceId == null || deviceType == null || deviceName == null) {
            return
        }

        deviceAPI?.createLog(LogRequest(
                message = message,
                score = logLevel.score,
                deviceId = deviceId,
                deviceType = deviceType,
                deviceName = deviceName))?.enqueue { }
    }
}