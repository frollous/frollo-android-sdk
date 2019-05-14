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

import us.frollo.frollosdk.model.api.bills.BillPaymentResponse
import us.frollo.frollosdk.model.api.bills.BillResponse
import us.frollo.frollosdk.model.coredata.bills.BillFrequency
import us.frollo.frollosdk.model.coredata.bills.BillPaymentStatus
import us.frollo.frollosdk.model.coredata.bills.BillStatus
import us.frollo.frollosdk.model.coredata.bills.BillType
import us.frollo.frollosdk.testutils.randomBoolean
import us.frollo.frollosdk.testutils.randomNumber
import us.frollo.frollosdk.testutils.randomString
import java.math.BigDecimal
import kotlin.random.Random

internal fun testBillResponseData(billId: Long? = null, merchantId: Long? = null, transactionCategoryId: Long? = null, accountId: Long? = null): BillResponse {
    val category = BillResponse.Category(
            id = transactionCategoryId ?: randomNumber().toLong(),
            name = randomString(20))

    val merchant = BillResponse.Merchant(
            id = merchantId ?: randomNumber().toLong(),
            name = randomString(20))

    return BillResponse(
            billId = billId ?: randomNumber().toLong(),
            name = randomString(50),
            description = randomString(50),
            billType = BillType.values()[Random.nextInt(BillType.values().size)],
            status = BillStatus.values()[Random.nextInt(BillStatus.values().size)],
            lastAmount = BigDecimal("101.23"),
            dueAmount = BigDecimal("79.65"),
            averageAmount = BigDecimal("99.89"),
            frequency = BillFrequency.values()[Random.nextInt(BillFrequency.values().size)],
            paymentStatus = BillPaymentStatus.values()[Random.nextInt(BillPaymentStatus.values().size)],
            lastPaymentDate = "2018-12-01",
            nextPaymentDate = "2019-01-01",
            category = category,
            merchant = merchant,
            accountId = accountId ?: randomNumber().toLong(),
            notes = randomString(200))
}

internal fun testBillPaymentResponseData(billPaymentId: Long? = null, billId: Long? = null, date: String? = null): BillPaymentResponse {
    return BillPaymentResponse(
            billPaymentId = billPaymentId ?: randomNumber().toLong(),
            billId = billId ?: randomNumber().toLong(),
            name = randomString(50),
            amount = BigDecimal("70.05"),
            frequency = BillFrequency.values()[Random.nextInt(BillFrequency.values().size)],
            paymentStatus = BillPaymentStatus.values()[Random.nextInt(BillPaymentStatus.values().size)],
            date = date ?: "2018-12-01",
            merchantId = randomNumber().toLong(),
            unpayable = randomBoolean())
}