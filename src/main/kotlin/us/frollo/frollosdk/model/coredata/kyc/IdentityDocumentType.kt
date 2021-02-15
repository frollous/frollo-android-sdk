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

package us.frollo.frollosdk.model.coredata.kyc

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.extensions.serializedName

/** Type of an Identity Document */
enum class IdentityDocumentType {

    /** ID Type OTHER */
    @SerializedName("OTHER") OTHER,

    /** ID Type DRIVERS LICENCE */
    @SerializedName("DRIVERS_LICENCE") DRIVERS_LICENCE,

    /** ID Type PASSPORT */
    @SerializedName("PASSPORT") PASSPORT,

    /** ID Type VISA */
    @SerializedName("VISA") VISA,

    /** ID Type IMMIGRATION */
    @SerializedName("IMMIGRATION") IMMIGRATION,

    /** ID Type NATIONAL ID */
    @SerializedName("NATIONAL_ID") NATIONAL_ID,

    /** ID Type TAX ID */
    @SerializedName("TAX_ID") TAX_ID,

    /** ID Type NATIONAL HEALTH ID */
    @SerializedName("NATIONAL_HEALTH_ID") NATIONAL_HEALTH_ID,

    /** ID Type CONCESSION */
    @SerializedName("CONCESSION") CONCESSION,

    /** ID Type HEALTH CONCESSION */
    @SerializedName("HEALTH_CONCESSION") HEALTH_CONCESSION,

    /** ID Type PENSION */
    @SerializedName("PENSION") PENSION,

    /** ID Type MILITARY ID */
    @SerializedName("MILITARY_ID") MILITARY_ID,

    /** ID Type BIRTH CERTIFICATE */
    @SerializedName("BIRTH_CERT") BIRTH_CERTIFICATE,

    /** ID Type CITIZENSHIP */
    @SerializedName("CITIZENSHIP") CITIZENSHIP,

    /** ID Type MARRIAGE CERTIFICATE */
    @SerializedName("MARRIAGE_CERT") MARRIAGE_CERTIFICATE,

    /** ID Type DEATH CERTIFICATE */
    @SerializedName("DEATH_CERT") DEATH_CERTIFICATE,

    /** ID Type NAME CHANGE */
    @SerializedName("NAME_CHANGE") NAME_CHANGE,

    /** ID Type UTILITY BILL */
    @SerializedName("UTILITY_BILL") UTILITY_BILL,

    /** ID Type BANK STATEMENT */
    @SerializedName("BANK_STATEMENT") BANK_STATEMENT,

    /** ID Type INTENT PROOF */
    @SerializedName("INTENT_PROOF") INTENT_PROOF,

    /** ID Type ATTESTATION */
    @SerializedName("ATTESTATION") ATTESTATION,

    /** ID Type SELF IMAGE */
    @SerializedName("SELF_IMAGE") SELF_IMAGE,

    /** ID Type EMAIL ADDRESS */
    @SerializedName("EMAIL_ADDRESS") EMAIL_ADDRESS,

    /** ID Type MSISDN */
    @SerializedName("MSISDN") MSISDN,

    /** ID Type DEVICE */
    @SerializedName("DEVICE") DEVICE;

    /** Enum to serialized string */
    // This override MUST be used for this enum to work with Retrofit @Path or @Query parameters
    override fun toString(): String =
        // Try to get the annotation value if available instead of using plain .toString()
        // Fallback to super.toString() in case annotation is not present/available
        serializedName() ?: super.toString()
}
