/*
 * Copyright 2019 Frollo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package us.frollo.frollosdk.model.coredata.aggregation.accounts

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import us.frollo.frollosdk.model.IAdapterModel
import us.frollo.frollosdk.model.coredata.aggregation.provideraccounts.RefreshStatus
import java.math.BigDecimal

// Declaring the ColumnInfo allows for the renaming of variables without
// implementing a database migration, as the column name would not change.

@Entity(tableName = "account",
        indices = [Index("account_id"),
            Index("provider_account_id")])
/**
 * Data representation of Account
 */
data class Account(

    /** Unique ID of the account */
    @PrimaryKey @ColumnInfo(name = "account_id") val accountId: Long,

    /** Name of the account */
    @ColumnInfo(name = "account_name") val accountName: String,

    /** Account number (optional) */
    @ColumnInfo(name = "account_number") val accountNumber: String?,

    /** Account BSB (optional) */
    @ColumnInfo(name = "bsb") val bsb: String?,

    /** Nickname given to the account for display and identification purposes (optional) */
    @ColumnInfo(name = "nick_name") val nickName: String?,

    /** Parent provider account ID */
    @ColumnInfo(name = "provider_account_id") val providerAccountId: Long,

    /** Name of the provider convenience property (optional) */
    @ColumnInfo(name = "provider_name") val providerName: String,

    /** Aggregator (optional) */
    @ColumnInfo(name = "aggregator") val aggregator: String?,

    /** ID of the aggregator */
    @ColumnInfo(name = "aggregator_id") val aggregatorId: Long,

    /** Account holder profile */
    @Embedded(prefix = "h_profile_") val holderProfile: HolderProfile?,

    /** Account status */
    @ColumnInfo(name = "account_status") val accountStatus: AccountStatus,

    /** Account attributes */
    @Embedded(prefix = "attr_") val attributes: AccountAttributes,

    /** Included in budget. Used to exclude accounts from counting towards the user's budgets */
    @ColumnInfo(name = "included") val included: Boolean,

    /** Favourited */
    @ColumnInfo(name = "favourite") val favourite: Boolean,

    /** Hidden. Used to hide the account in the UI */
    @ColumnInfo(name = "hidden") val hidden: Boolean,

    /** Refresh status */
    @Embedded(prefix = "r_status_") val refreshStatus: RefreshStatus?,

    /** Current balance (optional) */
    @Embedded(prefix = "c_balance_") val currentBalance: Balance?,

    /** Available balance (optional) */
    @Embedded(prefix = "a_balance_") val availableBalance: Balance?,

    /** Available cash (optional) */
    @Embedded(prefix = "a_cash_") val availableCash: Balance?,

    /** Available credit (optional) */
    @Embedded(prefix = "a_credit_") val availableCredit: Balance?,

    /** Total cash limit (optional) */
    @Embedded(prefix = "t_cash_") val totalCashLimit: Balance?,

    /** Total credit line (optional) */
    @Embedded(prefix = "t_credit_") val totalCreditLine: Balance?,

    /** Interest total (optional) */
    @Embedded(prefix = "int_total") val interestTotal: Balance?,

    /** APR percentage (optional) */
    @ColumnInfo(name = "apr") val apr: BigDecimal?,

    /** Interest rate (optional) */
    @ColumnInfo(name = "interest_rate") val interestRate: BigDecimal?,

    /** Amount due (optional) */
    @Embedded(prefix = "a_due_") val amountDue: Balance?,

    /** Minimum amount due (optional) */
    @Embedded(prefix = "m_amount_") val minimumAmountDue: Balance?,

    /** Last payment amount (optional) */
    @Embedded(prefix = "l_payment_") val lastPaymentAmount: Balance?,

    /**
     * Last payment date (optional)
     *
     * Date format for this field is ISO8601
     * example 2011-12-03T10:15:30+01:00
     */
    @ColumnInfo(name = "last_payment_date") val lastPaymentDate: String?,

    /**
     * Due date (optional)
     *
     * Date format for this field is ISO8601
     * example 2011-12-03T10:15:30+01:00
     */
    @ColumnInfo(name = "due_date") val dueDate: String?,

    /**
     * End date (optional)
     *
     * Date format for this field is yyyy-MM-dd
     * example 2011-12-03
     */
    @ColumnInfo(name = "end_date") val endDate: String?, // yyyy-MM-dd

    /** Balance details (optional) */
    @Embedded(prefix = "b_details_") val balanceDetails: BalanceDetails?,

    /** Goal IDs that are associated to this account */
    @ColumnInfo(name = "goal_ids") val goalIds: List<Long>?

) : IAdapterModel