package us.frollo.frollosdk.model.coredata.aggregation.accounts

import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName

/** Account attributes */
data class AccountAttributes(
        /** Account Type */
        @ColumnInfo(name = "account_type") @SerializedName("container") val accountType: AccountType,

        /** Account Sub Type */
        @ColumnInfo(name = "account_sub_type") @SerializedName("account_type") val accountSubType: AccountSubType,

        /** Account Group */
        @ColumnInfo(name = "account_group") @SerializedName("group") val group: AccountGroup,

        /** Account Classification */
        @ColumnInfo(name = "account_classification") @SerializedName("classification") val classification: AccountClassification?
)