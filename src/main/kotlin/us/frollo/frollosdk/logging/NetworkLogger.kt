/*
 * Copyright 2019 Frollo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package us.frollo.frollosdk.logging

import us.frollo.frollosdk.network.NetworkService
import us.frollo.frollosdk.network.api.DeviceAPI
import us.frollo.frollosdk.extensions.enqueue
import us.frollo.frollosdk.model.api.device.LogRequest

internal class NetworkLogger(
    private val network: NetworkService?,
    private val deviceId: String?,
    private val deviceType: String?,
    private val deviceName: String?
) : Logger() {

    private val deviceAPI: DeviceAPI? = network?.create(DeviceAPI::class.java)

    override fun writeMessage(message: String, logLevel: LogLevel) {
        val hasTokens = network?.authToken?.getAccessToken() != null ?: false
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