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

package us.frollo.frollosdk.model.coredata.aggregation.provideraccounts

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.extensions.serializedName

/** Sub status of what the issue with the account is. Use this to determine an appropriate action for the user to take to fix the issue. */
enum class AccountRefreshSubStatus {

    /** Success. Last update completed successfully */
    @SerializedName("success") SUCCESS,

    /** Partial Success. Usually associated with a [ProviderAccount], one or more Account refreshed successfully but others had issues */
    @SerializedName("partial_success") PARTIAL_SUCCESS,

    /** Input Required. Additional information is required from the user in the form of a loginForm on the [ProviderAccount] */
    @SerializedName("input_required") INPUT_REQUIRED,

    /** Provider Site Action. The user needs to go to the website of the Provider and complete some action. See [AccountRefreshAdditionalStatus] */
    @SerializedName("provider_site_action") PROVIDER_SITE_ACTION,

    /** Relogin Required. The login details were incorrect or changed. The user needs to re-enter their details in the Provider loginForm */
    @SerializedName("relogin_required") RELOGIN_REQUIRED,

    /** Temporary Failure. The refresh of the provider failed for a temporary reason. This will generally automatically be resolved at the next refresh. */
    @SerializedName("temporary_failure") TEMPORARY_FAILURE,

    /** Permanent Failure. The refresh failed permanently due to a reason that cannot be rectified. Usually due to an account being closed. */
    @SerializedName("permanent_failure") PERMANENT_FAILURE,

    /** Email Required. If the user is missing a last name on their profile, it needs to be updated. */
    @SerializedName("email_required") EMAIL_REQUIRED,

    /** Last Name Required. Used for credit score, if the user is missing a last name on their profile, it needs to be updated. */
    @SerializedName("last_name_required") LAST_NAME_REQUIRED;

    /** Enum to serialized string */
    //This override MUST be used for this enum to work with Retrofit @Path or @Query parameters
    override fun toString(): String =
    //Try to get the annotation value if available instead of using plain .toString()
    //Fallback to super.toString() in case annotation is not present/available
            serializedName() ?: super.toString()
}