package us.frollo.frollosdk.model.api.aggregation.accounts

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.model.coredata.aggregation.accounts.AccountSubType

internal data class AccountUpdateRequest(
        @SerializedName("account_type") val accountSubType: AccountSubType? = null,
        @SerializedName("favourite") val favourite: Boolean? = null,
        @SerializedName("hidden") val hidden: Boolean,
        @SerializedName("included") val included: Boolean,
        @SerializedName("nick_name") val nickName: String? = null
) {
    val valid: Boolean
        get() = !(hidden && included)
}