package us.frollo.frollosdk.model.api.aggregation.provideraccounts

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.model.coredata.aggregation.providers.ProviderLoginForm

internal data class ProviderAccountCreateRequest(
        @SerializedName("login_form") val loginForm: ProviderLoginForm,
        @SerializedName("provider_id") val providerID: Long)