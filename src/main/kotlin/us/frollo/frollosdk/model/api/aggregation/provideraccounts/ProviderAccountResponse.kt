package us.frollo.frollosdk.model.api.aggregation.provideraccounts

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.model.coredata.aggregation.provideraccounts.RefreshStatus
import us.frollo.frollosdk.model.coredata.aggregation.providers.*

internal data class ProviderAccountResponse(
        @SerializedName("id") val providerAccountId: Long,
        @SerializedName("provider_id") val providerId: Long,
        @SerializedName("editable") val editable: Boolean,
        @SerializedName("refresh_status") val refreshStatus: RefreshStatus?,
        @SerializedName("login_form") val loginForm: ProviderLoginForm?
)