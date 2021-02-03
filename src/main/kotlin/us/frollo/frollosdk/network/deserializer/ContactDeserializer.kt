package us.frollo.frollosdk.network.deserializer

import com.google.gson.Gson
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import us.frollo.frollosdk.model.coredata.contacts.Contact
import us.frollo.frollosdk.model.coredata.contacts.PaymentDetails
import us.frollo.frollosdk.model.coredata.contacts.PaymentMethod
import java.lang.reflect.Type
import java.text.ParseException

internal object ContactDeserializer : JsonDeserializer<Contact> {

    @Synchronized
    override fun deserialize(jsonElement: JsonElement, type: Type, jsonDeserializationContext: JsonDeserializationContext): Contact {
        try {
            val model = Gson().fromJson(jsonElement, Contact::class.java)
            val jsonObject = jsonElement.asJsonObject
            if (jsonObject.has("payment_details")) {
                val elem = jsonObject.get("payment_details")
                if (elem != null && !elem.isJsonNull) {
                    model.paymentDetails = when (model.paymentMethod) {
                        PaymentMethod.PAY_ANYONE -> Gson().fromJson(elem, PaymentDetails.PayAnyone::class.java)
                        PaymentMethod.BPAY -> Gson().fromJson(elem, PaymentDetails.Biller::class.java)
                        PaymentMethod.PAY_ID -> Gson().fromJson(elem, PaymentDetails.PayID::class.java)
                        PaymentMethod.INTERNATIONAL -> Gson().fromJson(elem, PaymentDetails.International::class.java)
                    }
                }
            }
            return model
        } catch (e: ParseException) {
            throw JsonParseException(e)
        }
    }
}
