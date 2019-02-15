package us.frollo.frollosdk.model.api.aggregation.accounts

import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.model.coredata.aggregation.accounts.*
import us.frollo.frollosdk.model.coredata.aggregation.provideraccounts.RefreshStatus
import java.math.BigDecimal

internal data class AccountResponse(
        @PrimaryKey
        @SerializedName("id") val accountId: Long,
        @SerializedName("account_name") val accountName: String,
        @SerializedName("account_number") val accountNumber: String?,
        @SerializedName("bsb") val bsb: String?,
        @SerializedName("nick_name") val nickName: String?,
        @SerializedName("provider_account_id") val providerAccountId: Long,
        @SerializedName("provider_name") val providerName: String,
        @SerializedName("aggregator") val aggregator: String?,
        @SerializedName("aggregator_id") val aggregatorId: Long,
        @SerializedName("holder_profile") val holderProfile: HolderProfile?,
        @SerializedName("account_status") val accountStatus: AccountStatus,
        @SerializedName("account_attributes") val attributes: AccountAttributes,
        @SerializedName("included") val included: Boolean,
        @SerializedName("favourite") val favourite: Boolean,
        @SerializedName("hidden") val hidden: Boolean,
        @SerializedName("refresh_status") val refreshStatus: RefreshStatus?,
        @SerializedName("current_balance") val currentBalance: Balance?,
        @SerializedName("available_balance") val availableBalance: Balance?,
        @SerializedName("available_cash") val availableCash: Balance?,
        @SerializedName("available_credit") val availableCredit: Balance?,
        @SerializedName("total_cash_limit") val totalCashLimit: Balance?,
        @SerializedName("total_credit_line") val totalCreditLine: Balance?,
        @SerializedName("interest_total") val interestTotal: Balance?,
        @SerializedName("apr") val apr: BigDecimal?,
        @SerializedName("interest_rate") val interestRate: BigDecimal?,
        @SerializedName("amount_due") val amountDue: Balance?,
        @SerializedName("minimum_amount_due") val minimumAmountDue: Balance?,
        @SerializedName("last_payment_amount") val lastPaymentAmount: Balance?,
        @SerializedName("last_payment_date") val lastPaymentDate: String?, // ISO8601 format Eg: 2011-12-03T10:15:30+01:00
        @SerializedName("due_date") val dueDate: String?, // ISO8601 format Eg: 2011-12-03T10:15:30+01:00
        @SerializedName("end_date") val endDate: String?, // yyyy-MM-dd
        @SerializedName("balance_details") val balanceDetails: BalanceDetails?
)