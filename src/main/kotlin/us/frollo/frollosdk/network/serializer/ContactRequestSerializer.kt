package us.frollo.frollosdk.network.serializer

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import us.frollo.frollosdk.model.api.contacts.ContactCreateUpdateRequest
import us.frollo.frollosdk.model.coredata.contacts.PaymentDetails
import us.frollo.frollosdk.model.coredata.contacts.PaymentMethod
import java.lang.reflect.Type

internal object ContactRequestSerializer : JsonSerializer<ContactCreateUpdateRequest> {

    @Synchronized
    override fun serialize(src: ContactCreateUpdateRequest?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        val jsonObject = Gson().toJsonTree(src, typeOfSrc).asJsonObject
        jsonObject.remove("payment_details") // Remove payment_details to ensure there is no duplicate when we add below
        val paymentDetailsElement = when (src?.paymentMethod) {
            PaymentMethod.PAY_ANYONE -> Gson().toJsonTree(src.paymentDetails, PaymentDetails.PayAnyone::class.java)
            PaymentMethod.BPAY -> Gson().toJsonTree(src.paymentDetails, PaymentDetails.Biller::class.java)
            PaymentMethod.PAY_ID -> Gson().toJsonTree(src.paymentDetails, PaymentDetails.PayID::class.java)
            PaymentMethod.INTERNATIONAL -> Gson().toJsonTree(src.paymentDetails, PaymentDetails.International::class.java)
            else -> null
        }
        paymentDetailsElement?.let {
            jsonObject.add("payment_details", it)
        }
        return jsonObject
    }
}
