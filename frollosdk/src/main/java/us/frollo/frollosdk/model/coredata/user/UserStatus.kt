package us.frollo.frollosdk.model.coredata.user

import com.google.gson.annotations.SerializedName

enum class UserStatus {
    @SerializedName("registered") REGISTERED,
    @SerializedName("account_added") ACCOUNT_ADDED,
    @SerializedName("budget_ready") BUDGET_READY,
    @SerializedName("active") ACTIVE,
    @SerializedName("inactive") INACTIVE
}