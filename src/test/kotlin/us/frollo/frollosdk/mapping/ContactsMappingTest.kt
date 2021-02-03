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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import us.frollo.frollosdk.model.coredata.contacts.PaymentDetails
import us.frollo.frollosdk.model.coredata.contacts.PaymentMethod
import us.frollo.frollosdk.model.testContactResponseData

class ContactsMappingTest {

    @Test
    fun testContactResponseToContact() {
        var response = testContactResponseData(contactId = 12345, paymentMethod = PaymentMethod.PAY_ANYONE)
        var model = response.toContact()
        assertEquals(12345L, model.contactId)
        assertTrue(model.paymentDetails is PaymentDetails.PayAnyone)
        assertTrue((model.paymentDetails as PaymentDetails.PayAnyone).accountHolder.isNotBlank())

        response = testContactResponseData(contactId = 12345, paymentMethod = PaymentMethod.BPAY)
        model = response.toContact()
        assertEquals(12345L, model.contactId)
        assertTrue(model.paymentDetails is PaymentDetails.Biller)
        assertTrue((model.paymentDetails as PaymentDetails.Biller).crn.isNotBlank())

        response = testContactResponseData(contactId = 12345, paymentMethod = PaymentMethod.PAY_ID)
        model = response.toContact()
        assertEquals(12345L, model.contactId)
        assertTrue(model.paymentDetails is PaymentDetails.PayID)
        assertTrue((model.paymentDetails as PaymentDetails.PayID).payId.isNotBlank())

        response = testContactResponseData(contactId = 12345, paymentMethod = PaymentMethod.INTERNATIONAL)
        model = response.toContact()
        assertEquals(12345L, model.contactId)
        assertTrue(model.paymentDetails is PaymentDetails.International)
        assertTrue((model.paymentDetails as PaymentDetails.International).beneficiary.country.isNotBlank())
        assertTrue((model.paymentDetails as PaymentDetails.International).bankDetails.country.isNotBlank())
    }
}
