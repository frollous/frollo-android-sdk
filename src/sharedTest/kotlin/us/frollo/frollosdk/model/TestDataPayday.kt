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

package us.frollo.frollosdk.model

import us.frollo.frollosdk.model.api.payday.PaydayResponse
import us.frollo.frollosdk.model.coredata.payday.PaydayFrequency
import us.frollo.frollosdk.model.coredata.payday.PaydayStatus
import us.frollo.frollosdk.testutils.randomElement

internal fun testPaydayResponseData(
    frequency: PaydayFrequency? = null,
    status: PaydayStatus? = null
): PaydayResponse {
    return PaydayResponse(
        status = status ?: PaydayStatus.values().randomElement(),
        frequency = frequency ?: PaydayFrequency.values().randomElement(),
        nextDate = "2021-01-31",
        previousDate = "2020-12-31"
    )
}
