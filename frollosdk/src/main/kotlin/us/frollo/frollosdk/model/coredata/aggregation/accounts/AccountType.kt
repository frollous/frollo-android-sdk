package us.frollo.frollosdk.model.coredata.aggregation.accounts

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.extensions.serializedName

enum class AccountType {
    @SerializedName("bank_account") BANK_ACCOUNT,
    @SerializedName("savings") SAVINGS,
    @SerializedName("emergency_fund") EMERGENCY_FUND,
    @SerializedName("term_deposit") TERM_DEPOSIT,
    @SerializedName("bills") BILLS,
    @SerializedName("offset") OFFSET,
    @SerializedName("travel") TRAVEL,
    @SerializedName("prepaid") PREPAID,
    @SerializedName("balance_transfer_card") BALANCE_TRANSFER_CARD,
    @SerializedName("rewards_card") REWARDS_CARD,
    @SerializedName("credit_card") CREDIT_CARD,
    @SerializedName("super_annuation") SUPER_ANNUATION,
    @SerializedName("shares") SHARES,
    @SerializedName("business") BUSINESS,
    @SerializedName("bonds") BONDS,
    @SerializedName("pension") PENSION,
    @SerializedName("mortgage") MORTGAGE,
    @SerializedName("mortgage_fixed") MORTGAGE_FIXED,
    @SerializedName("mortgage_variable") MORTGAGE_VARIABLE,
    @SerializedName("investment_home_loan_fixed") INVESTMENT_HOME_LOAN_FIXED,
    @SerializedName("investment_home_loan_variable") INVESTMENT_HOME_LOAN_VARIABLE,
    @SerializedName("student_loan") STUDENT_LOAN,
    @SerializedName("car_loan") CAR_LOAN,
    @SerializedName("line_of_credit") LINE_OF_CREDIT,
    @SerializedName("p2p_lending") P2P_LENDING,
    @SerializedName("personal") PERSONAL,
    @SerializedName("auto_insurance") AUTO_INSURANCE,
    @SerializedName("health_insurance") HEALTH_INSURANCE,
    @SerializedName("home_insurance") HOME_INSURANCE,
    @SerializedName("life_insurance") LIFE_INSURANCE,
    @SerializedName("travel_insurance") TRAVEL_INSURANCE,
    @SerializedName("insurance") INSURANCE,
    @SerializedName("reward") REWARD,
    @SerializedName("credit_score") CREDIT_SCORE,
    @SerializedName("health_score") HEALTH_SCORE,
    @SerializedName("other") OTHER;

    //This override MUST be used for this enum to work with Retrofit @Path or @Query parameters
    override fun toString(): String =
    //Try to get the annotation value if available instead of using plain .toString()
    //Fallback to super.toString() in case annotation is not present/available
            serializedName() ?: super.toString()
}


