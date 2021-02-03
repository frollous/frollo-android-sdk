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

import us.frollo.frollosdk.model.api.contacts.ContactResponse
import us.frollo.frollosdk.model.coredata.contacts.BankAddress
import us.frollo.frollosdk.model.coredata.contacts.BankDetails
import us.frollo.frollosdk.model.coredata.contacts.Beneficiary
import us.frollo.frollosdk.model.coredata.contacts.CRNType
import us.frollo.frollosdk.model.coredata.contacts.PayIDType
import us.frollo.frollosdk.model.coredata.contacts.PaymentDetails
import us.frollo.frollosdk.model.coredata.contacts.PaymentMethod
import us.frollo.frollosdk.testutils.randomElement
import us.frollo.frollosdk.testutils.randomNumber
import us.frollo.frollosdk.testutils.randomString

internal fun testContactResponseData(
    contactId: Long? = null,
    paymentMethod: PaymentMethod? = null
): ContactResponse {
    val method = paymentMethod ?: PaymentMethod.values().randomElement()
    return ContactResponse(
        contactId = contactId ?: randomNumber().toLong(),
        createdDate = "2019-01-02",
        modifiedDate = "2019-01-02",
        verified = true,
        relatedProviderAccountIds = listOf(100L, 101L),
        name = randomString(20),
        nickName = randomString(20),
        description = randomString(20),
        paymentMethod = method,
        paymentDetails = testPaymentDetailsData(method)
    )
}

internal fun testPaymentDetailsData(paymentMethod: PaymentMethod): PaymentDetails {
    return when (paymentMethod) {
        PaymentMethod.PAY_ANYONE -> testPayAnyoneDetailsData()
        PaymentMethod.BPAY -> testBillerDetailsData()
        PaymentMethod.PAY_ID -> testPayIDDetailsData()
        PaymentMethod.INTERNATIONAL -> testInternationalDetailsData()
    }
}

internal fun testPayAnyoneDetailsData(): PaymentDetails.PayAnyone {
    return PaymentDetails.PayAnyone(
        accountHolder = randomString(20),
        accountNumber = randomString(20),
        bsb = randomString(20)
    )
}

internal fun testBillerDetailsData(): PaymentDetails.Biller {
    return PaymentDetails.Biller(
        billerCode = randomString(20),
        crn = randomString(20),
        billerName = randomString(20),
        crnType = CRNType.values().randomElement()
    )
}

internal fun testPayIDDetailsData(): PaymentDetails.PayID {
    return PaymentDetails.PayID(
        name = randomString(20),
        payId = randomString(20),
        idType = PayIDType.values().randomElement()
    )
}

internal fun testInternationalDetailsData(): PaymentDetails.International {
    return PaymentDetails.International(
        beneficiary = Beneficiary(
            name = randomString(20),
            country = randomString(20),
            message = randomString(20)
        ),
        bankDetails = BankDetails(
            country = randomString(20),
            accountNumber = randomString(20),
            bankAddress = BankAddress(
                address = randomString(20)
            ),
            bic = randomString(20),
            fedWireNumber = randomString(20),
            sortCode = randomString(20),
            chipNumber = randomString(20),
            routingNumber = randomString(20),
            legalEntityIdentifier = randomString(20)
        )
    )
}
