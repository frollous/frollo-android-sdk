package us.frollo.frollosdk.model.coredata.aggregation.accounts

import androidx.room.*
import us.frollo.frollosdk.model.IAdapterModel
import us.frollo.frollosdk.model.coredata.aggregation.provideraccounts.RefreshStatus
import java.math.BigDecimal
import java.util.*

// Declaring the ColumnInfo allows for the renaming of variables without
// implementing a database migration, as the column name would not change.

@Entity(tableName = "account",
        indices = [Index("account_id"),
                   Index("provider_account_id")])

data class Account(

        @PrimaryKey
        @ColumnInfo(name = "account_id") val accountId: Long,

        @ColumnInfo(name = "account_name") val accountName: String,

        @ColumnInfo(name = "account_number") val accountNumber: String?,

        @ColumnInfo(name = "bsb") val bsb: String?,

        @ColumnInfo(name = "nick_name") val nickName: String?,

        @ColumnInfo(name = "provider_account_id") val providerAccountId: Long,

        @ColumnInfo(name = "provider_name") val providerName: String,

        @ColumnInfo(name = "aggregator") val aggregator: String?,

        @ColumnInfo(name = "aggregator_id") val aggregatorId: Long,

        @Embedded(prefix = "h_profile_") val holderProfile: HolderProfile?,

        @ColumnInfo(name = "account_status") val accountStatus: AccountStatus,

        @Embedded(prefix = "attr_") val attributes: AccountAttributes,

        @ColumnInfo(name = "included") val included: Boolean,

        @ColumnInfo(name = "favourite") val favourite: Boolean,

        @ColumnInfo(name = "hidden") val hidden: Boolean,

        @Embedded(prefix = "r_status_") val refreshStatus: RefreshStatus?,

        @Embedded(prefix = "c_balance_") val currentBalance: Balance?,

        @Embedded(prefix = "a_balance_") val availableBalance: Balance?,

        @Embedded(prefix = "a_cash_") val availableCash: Balance?,

        @Embedded(prefix = "a_credit_") val availableCredit: Balance?,

        @Embedded(prefix = "t_cash_") val totalCashLimit: Balance?,

        @Embedded(prefix = "t_credit_") val totalCreditLine: Balance?,

        @Embedded(prefix = "int_total") val interestTotal: Balance?,

        @ColumnInfo(name = "apr") val apr: BigDecimal?,

        @ColumnInfo(name = "interest_rate") val interestRate: BigDecimal?,

        @Embedded(prefix = "a_due_") val amountDue: Balance?,

        @Embedded(prefix = "m_amount_") val minimumAmountDue: Balance?,

        @Embedded(prefix = "l_payment_") val lastPaymentAmount: Balance?,

        @ColumnInfo(name = "last_payment_date") val lastPaymentDate: String?, // ISO8601 format Eg: 2011-12-03T10:15:30+01:00

        @ColumnInfo(name = "due_date") val dueDate: String?, // ISO8601 format Eg: 2011-12-03T10:15:30+01:00

        @ColumnInfo(name = "end_date") val endDate: String?, // yyyy-MM-dd

        @Embedded(prefix = "b_details_") val balanceDetails: BalanceDetails?

): IAdapterModel