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

package us.frollo.frollosdk.model.coredata.aggregation.providers

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.extensions.serializedName

/**
 * Represents the CDRProduct that belongs to a Provider
 */
data class CDRProduct(

    /** ID of the product */
    @SerializedName("id") val productId: Long,

    /** Brand of the product */
    @SerializedName("brand") val brand: String?,

    /** Brand name of the product */
    @SerializedName("brand_name") val brandName: String?,

    /** External ID of the product */
    @SerializedName("external_id") val externalId: Long?,

    /** Fees URL of the product */
    @SerializedName("fees_uri") val feesUrl: String?,

    /** Terms URL of the product */
    @SerializedName("terms_uri") val termsUrl: String?,

    /** Description of the product */
    @SerializedName("description") val description: String?,

    /** Name of the product */
    @SerializedName("name") val name: String?,

    /** Category of the product */
    @SerializedName("product_category") val productCategory: CDRProductCategory?,

    /** ProviderID that product belongs to */
    @SerializedName("provider_id") val providerId: Long
)

/**
 * Data representation of CDR Product Category that belongs to a Provider
 */
enum class CDRProductCategory {

    /** Transaction and saving accounts */
    @SerializedName("TRANS_AND_SAVINGS_ACCOUNTS") TRANSACTION_AND_SAVINGS_ACCOUNTS,

    /** Consent is still pending */
    @SerializedName("TERM_DEPOSITS") TERM_DEPOSITS,

    /** Term deposits */
    @SerializedName("TRAVEL_CARDS") TRAVEL_CARDS,

    /** Regulated trust accounts */
    @SerializedName("REGULATED_TRUST_ACCOUNTS") REGULATED_TRUST_ACCOUNTS,

    /** Residential mortgages */
    @SerializedName("RESIDENTIAL_MORTGAGES") RESIDENTIAL_MORTGAGES,

    /** Credit and charge cards */
    @SerializedName("CRED_AND_CHRG_CARDS") CREDIT_AND_CHARGE_CARDS,

    /** Personal loans */
    @SerializedName("PERS_LOANS") PERSONAL_LOANS,

    /** Margin loans */
    @SerializedName("MARGIN_LOANS") MARGIN_LOANS,

    /** Leases */
    @SerializedName("LEASES") LEASES,

    /** Trade finance */
    @SerializedName("TRADE_FINANCE") TRADE_FINANCE,

    /** Overdrafts */
    @SerializedName("OVERDRAFTS") OVERDRAFTS,

    /** Business loans */
    @SerializedName("BUSINESS_LOANS") BUSINESS_LOANS;

    /** Enum to serialized string */
    // This override MUST be used for this enum to work with Retrofit @Path or @Query parameters
    override fun toString(): String =
        // Try to get the annotation value if available instead of using plain .toString()
        // Fallback to super.toString() in case annotation is not present/available
        serializedName() ?: super.toString()
}
