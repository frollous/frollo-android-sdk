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

/** What account group the account should appear in */
enum class AccountGroup {

    /** Bank accounts */
    @SerializedName("bank") BANK,

    /** Savings accounts */
    @SerializedName("savings") SAVINGS,

    /** Credit cards */
    @SerializedName("credit_card") CREDIT_CARD,

    /** Super Annuation */
    @SerializedName("super_annuation") SUPER_ANNUATION,

    /** Investments */
    @SerializedName("investment") INVESTMENT,

    /** Loans and mortgages */
    @SerializedName("loan") LOAN,

    /** Insurance */
    @SerializedName("insurance") INSURANCE,

    /** Rewards and Loyalty */
    @SerializedName("reward") REWARD,

    /** Scores */
    @SerializedName("score") SCORE,

    /** Custom section (as defined per tenant) */
    @SerializedName("custom") CUSTOM,

    /** Other or unknown */
    @SerializedName("other") OTHER;

    /** Enum to serialized string */
    // This override MUST be used for this enum to work with Retrofit @Path or @Query parameters
    override fun toString(): String =
        // Try to get the annotation value if available instead of using plain .toString()
        // Fallback to super.toString() in case annotation is not present/available
        serializedName() ?: super.toString()
}
