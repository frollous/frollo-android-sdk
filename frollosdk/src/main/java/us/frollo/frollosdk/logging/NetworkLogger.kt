package us.frollo.frollosdk.logging

import us.frollo.frollosdk.data.remote.NetworkService

internal class NetworkLogger(network: NetworkService?) : Logger() {

    override fun writeMessage(message: String, level: LogLevel) {

    }
}