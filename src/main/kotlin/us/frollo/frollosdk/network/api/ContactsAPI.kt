package us.frollo.frollosdk.network.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.QueryMap
import us.frollo.frollosdk.model.api.contacts.ContactCreateUpdateRequest
import us.frollo.frollosdk.model.api.contacts.ContactResponse
import us.frollo.frollosdk.model.api.shared.PaginatedResponse

internal interface ContactsAPI {

    companion object {
        const val URL_CONTACTS = "contacts"
        const val URL_CONTACT = "contacts/{contact_id}"
    }

    @GET(URL_CONTACTS)
    fun fetchContacts(@QueryMap options: Map<String, String>): Call<PaginatedResponse<ContactResponse>>

    @GET(URL_CONTACT)
    fun fetchContact(@Path("contact_id") contactId: Long): Call<ContactResponse>

    @POST(URL_CONTACTS)
    fun createContact(@Body request: ContactCreateUpdateRequest): Call<ContactResponse>

    @PUT(URL_CONTACT)
    fun updateContact(@Path("contact_id") contactId: Long, @Body request: ContactCreateUpdateRequest): Call<ContactResponse>

    @DELETE(URL_CONTACT)
    fun deleteContact(@Path("contact_id") contactId: Long): Call<Void>
}
