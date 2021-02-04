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

package us.frollo.frollosdk.network.deserializer

import android.app.Application
import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.JsonParser
import org.junit.Assert.assertEquals
import org.junit.Test
import us.frollo.frollosdk.model.api.contacts.ContactResponse
import us.frollo.frollosdk.model.coredata.contacts.CRNType
import us.frollo.frollosdk.model.coredata.contacts.PayIDType
import us.frollo.frollosdk.model.coredata.contacts.PaymentDetails
import us.frollo.frollosdk.model.coredata.contacts.PaymentMethod
import us.frollo.frollosdk.test.R
import us.frollo.frollosdk.testutils.readStringFromJson

class ContactResponseDeserializerTest {

    val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application

    @Test
    fun testDeserializePayAnyoneContact() {
        val jsonString = readStringFromJson(app, R.raw.contact_update_pay_anyone_contact)
        val jsonObject = JsonParser().parse(jsonString).asJsonObject
        val contactResponse: ContactResponse = ContactResponseDeserializer.deserialize(jsonObject, ContactResponse::class.java, null)
        assertEquals(1L, contactResponse.contactId)
        assertEquals("Johnathan", contactResponse.name)
        assertEquals("Johnny Boy", contactResponse.nickName)
        assertEquals(PaymentMethod.PAY_ANYONE, contactResponse.paymentMethod)
        assertEquals("2020-12-07T13:59:35.677+11:00", contactResponse.createdDate)
        assertEquals("2020-12-07T13:59:35.677+11:00", contactResponse.modifiedDate)
        assertEquals(false, contactResponse.verified)
        val paymentDetails = contactResponse.paymentDetails as PaymentDetails.PayAnyone
        assertEquals("Mr Johnathan Smith", paymentDetails.accountHolder)
        assertEquals("12345678", paymentDetails.accountNumber)
        assertEquals("100-123", paymentDetails.bsb)
    }

    @Test
    fun testDeserializeBPayContact() {
        val jsonString = readStringFromJson(app, R.raw.contact_update_bpay_contact)
        val jsonObject = JsonParser().parse(jsonString).asJsonObject
        val contactResponse: ContactResponse = ContactResponseDeserializer.deserialize(jsonObject, ContactResponse::class.java, null)
        assertEquals(9L, contactResponse.contactId)
        assertEquals("Tenstra Inc", contactResponse.name)
        assertEquals("Tenstra", contactResponse.nickName)
        assertEquals(PaymentMethod.BPAY, contactResponse.paymentMethod)
        assertEquals("2020-12-07T14:29:21.112+11:00", contactResponse.createdDate)
        assertEquals("2020-12-07T14:29:21.112+11:00", contactResponse.modifiedDate)
        assertEquals(false, contactResponse.verified)
        val paymentDetails = contactResponse.paymentDetails as PaymentDetails.Biller
        assertEquals("2275362", paymentDetails.billerCode)
        assertEquals("723647803", paymentDetails.crn)
        assertEquals("Tenstra Inc", paymentDetails.billerName)
        assertEquals(CRNType.FIXED, paymentDetails.crnType)
    }

    @Test
    fun testDeserializePayIDContact() {
        val jsonString = readStringFromJson(app, R.raw.contact_update_pay_id_contact)
        val jsonObject = JsonParser().parse(jsonString).asJsonObject
        val contactResponse: ContactResponse = ContactResponseDeserializer.deserialize(jsonObject, ContactResponse::class.java, null)
        assertEquals(4L, contactResponse.contactId)
        assertEquals("Johnathan Gilbert", contactResponse.name)
        assertEquals("Johnny", contactResponse.nickName)
        assertEquals(PaymentMethod.PAY_ID, contactResponse.paymentMethod)
        assertEquals("2020-12-07T13:55:49.240+11:00", contactResponse.createdDate)
        assertEquals("2020-12-08T10:24:42.128+11:00", contactResponse.modifiedDate)
        assertEquals(false, contactResponse.verified)
        val paymentDetails = contactResponse.paymentDetails as PaymentDetails.PayID
        assertEquals("j.gilbert@frollo.com", paymentDetails.payId)
        assertEquals("J GILBERT", paymentDetails.name)
        assertEquals(PayIDType.EMAIL, paymentDetails.type)
    }

    @Test
    fun testDeserializeInternationalContact() {
        val jsonString = readStringFromJson(app, R.raw.contact_update_international_contact)
        val jsonObject = JsonParser().parse(jsonString).asJsonObject
        val contactResponse: ContactResponse = ContactResponseDeserializer.deserialize(jsonObject, ContactResponse::class.java, null)
        assertEquals(9L, contactResponse.contactId)
        assertEquals("Anne Maria", contactResponse.name)
        assertEquals("Mary", contactResponse.nickName)
        assertEquals(PaymentMethod.INTERNATIONAL, contactResponse.paymentMethod)
        assertEquals("2020-12-07T16:35:25.741+11:00", contactResponse.createdDate)
        assertEquals("2020-12-08T10:27:28.999+11:00", contactResponse.modifiedDate)
        assertEquals(false, contactResponse.verified)
        val paymentDetails = contactResponse.paymentDetails as PaymentDetails.International
        assertEquals("Anne Maria", paymentDetails.beneficiary.name)
        assertEquals("New Zeland", paymentDetails.beneficiary.country)
        assertEquals("Test message new", paymentDetails.beneficiary.message)
        assertEquals("New Zeland", paymentDetails.bankDetails.country)
        assertEquals("12345666", paymentDetails.bankDetails.accountNumber)
        assertEquals("ABC 666", paymentDetails.bankDetails.bankAddress?.address)
        assertEquals("777", paymentDetails.bankDetails.bic)
        assertEquals("1234566", paymentDetails.bankDetails.fedWireNumber)
        assertEquals("666", paymentDetails.bankDetails.sortCode)
        assertEquals("555", paymentDetails.bankDetails.chipNumber)
        assertEquals("444", paymentDetails.bankDetails.routingNumber)
        assertEquals("123666", paymentDetails.bankDetails.legalEntityIdentifier)
    }
}
