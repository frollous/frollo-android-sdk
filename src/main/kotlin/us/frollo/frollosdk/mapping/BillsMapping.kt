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

package us.frollo.frollosdk.mapping

import us.frollo.frollosdk.model.api.bills.BillPaymentResponse
import us.frollo.frollosdk.model.api.bills.BillResponse
import us.frollo.frollosdk.model.coredata.bills.Bill
import us.frollo.frollosdk.model.coredata.bills.BillPayment

internal fun BillResponse.toBill(): Bill =
        Bill(
                billId = billId,
                name = name,
                description = description,
                billType = billType,
                status = status,
                lastAmount = lastAmount,
                dueAmount = dueAmount,
                averageAmount = averageAmount,
                frequency = frequency,
                paymentStatus = paymentStatus,
                lastPaymentDate = lastPaymentDate,
                nextPaymentDate = nextPaymentDate,
                categoryId = category?.id,
                merchantId = merchant?.id,
                accountId = accountId,
                notes = notes)

internal fun BillPaymentResponse.toBillPayment(): BillPayment =
        BillPayment(
                billPaymentId = billPaymentId,
                billId = billId,
                name = name,
                merchantId = merchantId,
                date = date,
                paymentStatus = paymentStatus,
                frequency = frequency,
                amount = amount,
                unpayable = unpayable)