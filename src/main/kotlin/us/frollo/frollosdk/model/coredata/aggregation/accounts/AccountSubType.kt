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

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.extensions.serializedName

/** Sub type of the account indicating more detail what the account is */
enum class AccountSubType {

    /** Bank account */
    @SerializedName("bank_account") BANK_ACCOUNT,

    /** Savings account */
    @SerializedName("savings") SAVINGS,

    /** Emergency savings fund */
    @SerializedName("emergency_fund") EMERGENCY_FUND,

    /** Term deposit */
    @SerializedName("term_deposit") TERM_DEPOSIT,

    /** Bills fund */
    @SerializedName("bills") BILLS,

    /** Offset account */
    @SerializedName("offset") OFFSET,

    /** Travel Card */
    @SerializedName("travel") TRAVEL,

    /** Prepaid Card */
    @SerializedName("prepaid") PREPAID,

    /** Balance transfer card */
    @SerializedName("balance_transfer_card") BALANCE_TRANSFER_CARD,

    /** Reward points card */
    @SerializedName("rewards_card") REWARDS_CARD,

    /** Generic credit card */
    @SerializedName("credit_card") CREDIT_CARD,

    /** Super Annuation */
    @SerializedName("super_annuation") SUPER_ANNUATION,

    /** Shares and Stocks */
    @SerializedName("shares") SHARES,

    /** Business account */
    @SerializedName("business") BUSINESS,

    /** Bonds */
    @SerializedName("bonds") BONDS,

    /** Pension */
    @SerializedName("pension") PENSION,

    /** Generic Mortgage */
    @SerializedName("mortgage") MORTGAGE,

    /** Mortgage - Fixed Rate */
    @SerializedName("mortgage_fixed") MORTGAGE_FIXED,

    /** Mortgage - Variable Rate */
    @SerializedName("mortgage_variable") MORTGAGE_VARIABLE,

    /** Investment Home Loan - Fixed Rate */
    @SerializedName("investment_home_loan_fixed") INVESTMENT_HOME_LOAN_FIXED,

    /** Investment Home Loan - Variable */
    @SerializedName("investment_home_loan_variable") INVESTMENT_HOME_LOAN_VARIABLE,

    /** Student Loan */
    @SerializedName("student_loan") STUDENT_LOAN,

    /** Car Loan */
    @SerializedName("car_loan") CAR_LOAN,

    /** Line of Credit */
    @SerializedName("line_of_credit") LINE_OF_CREDIT,

    /** Peer to Peer Loan */
    @SerializedName("p2p_lending") P2P_LENDING,

    /** Personal Loan */
    @SerializedName("personal") PERSONAL,

    /** Car insurance */
    @SerializedName("auto_insurance") AUTO_INSURANCE,

    /** Health insurance */
    @SerializedName("health_insurance") HEALTH_INSURANCE,

    /** Home insurance */
    @SerializedName("home_insurance") HOME_INSURANCE,

    /** Life insurance */
    @SerializedName("life_insurance") LIFE_INSURANCE,

    /** Travel Insurance */
    @SerializedName("travel_insurance") TRAVEL_INSURANCE,

    /** Generic Insurance */
    @SerializedName("insurance") INSURANCE,

    /** Reward or Loyalty */
    @SerializedName("reward") REWARD,

    /** Credit Score */
    @SerializedName("credit_score") CREDIT_SCORE,

    /** Financial Health Score */
    @SerializedName("health_score") HEALTH_SCORE,

    /** Other or unknown sub type */
    @SerializedName("other") OTHER;

    /** Enum to serialized string */
    // This override MUST be used for this enum to work with Retrofit @Path or @Query parameters
    override fun toString(): String =
    // Try to get the annotation value if available instead of using plain .toString()
    // Fallback to super.toString() in case annotation is not present/available
            serializedName() ?: super.toString()
}
