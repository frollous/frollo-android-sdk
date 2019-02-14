package us.frollo.frollosdk.model.coredata.aggregation.accounts

import com.google.gson.annotations.SerializedName

data class AccountAttributes(
    @SerializedName("container") val container: AccountContainer,
    @SerializedName("account_type") val accountType: AccountType,
    @SerializedName("group") val group: AccountGroup,
    @SerializedName("classification") val classification: AccountClassification?
)