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

package us.frollo.frollosdk.model.api.shared

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.extensions.serializedName

/**
 * Frollo API Error Codes
 *
 * @param rawValue Frollo API Error Code raw value from the host
 */
enum class APIErrorCode(val rawValue: String) {
    // 400 Bad Request
    /** Invalid Value */
    @SerializedName("F0001") INVALID_VALUE("F0001"),
    /** Invalid Length */
    @SerializedName("F0002") INVALID_LENGTH("F0002"),
    /** Invalid Authorisation Header */
    @SerializedName("F0003") INVALID_AUTHORISATION_HEADER("F0003"),
    /** Invalid User Agent Header */
    @SerializedName("F0004") INVALID_USER_AGENT_HEADER("F0004"),
    /** Invalid Must Be Different */
    @SerializedName("F0005") INVALID_MUST_BE_DIFFERENT("F0005"),
    /** Invalid Over Limit */
    @SerializedName("F0006") INVALID_OVER_LIMIT("F0006"),
    /** Invalid Count */
    @SerializedName("F0007") INVALID_COUNT("F0007"),
    /** Migration Error */
    @SerializedName("F0012") MIGRATION_FAILED("F0012"),
    /** Aggregator Bad Request Received */
    @SerializedName("F0014") AGGREGATOR_BAD_REQUEST("F0014"),

    // 401 Not authenticated
    /** Invalid Access Token */
    @SerializedName("F0101") INVALID_ACCESS_TOKEN("F0101"),
    /** Invalid Refresh Token */
    @SerializedName("F0110") INVALID_REFRESH_TOKEN("F0110"),
    /** Invalid Username Password */
    @SerializedName("F0111") INVALID_USERNAME_PASSWORD("F0111"),
    /** Suspended User */
    @SerializedName("F0112") SUSPENDED_USER("F0112"),
    /** Suspended Device */
    @SerializedName("F0113") SUSPENDED_DEVICE("F0113"),
    /** Account Locked */
    @SerializedName("F0114") ACCOUNT_LOCKED("F0114"),

    // 403 Not authorised
    /** Unauthorised */
    @SerializedName("F0200") UNAUTHORISED("F0200"),

    // 404 Object not found
    /** Not Found */
    @SerializedName("F0300") NOT_FOUND("F0300"),

    // 409 Conflict
    /** Already Exists */
    @SerializedName("F0400") ALREADY_EXISTS("F0400"),

    // 500 Internal Server Error
    /** Aggregator Error */
    @SerializedName("F9000") AGGREGATOR_ERROR("F9000"),
    /** Unknown Server Error */
    @SerializedName("F9998") UNKNOWN_SERVER("F9998"),
    /** Internal Exception */
    @SerializedName("F9999") INTERNAL_EXCEPTION("F9999");

    /** Enum to serialized string */
    // This override MUST be used for this enum to work with Retrofit @Path or @Query parameters
    override fun toString(): String =
        // Try to get the annotation value if available instead of using plain .toString()
        // Fallback to super.toString() in case annotation is not present/available
        serializedName() ?: super.toString()

    companion object {
        /**
         * Get instance of APIErrorCode from error code raw value
         *
         * @param errorCode Error Code raw value from the host
         * @return Returns APIErrorCode  or null if no match
         */
        fun fromRawValue(errorCode: String): APIErrorCode? {
            return values().find { it.rawValue == errorCode }
        }
    }
}
