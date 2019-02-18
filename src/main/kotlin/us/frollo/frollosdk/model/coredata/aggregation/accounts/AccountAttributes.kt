package us.frollo.frollosdk.model.coredata.aggregation.accounts

import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName

data class AccountAttributes(
        @ColumnInfo(name = "account_type") @SerializedName("container") val accountType: AccountType,
        @ColumnInfo(name = "account_sub_type") @SerializedName("account_type") val accountSubType: AccountSubType,
        @ColumnInfo(name = "account_group") @SerializedName("group") val group: AccountGroup,
        @ColumnInfo(name = "account_classification") @SerializedName("classification") val classification: AccountClassification?
)