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

package us.frollo.frollosdk.model.api.user

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.model.coredata.user.Address
import us.frollo.frollosdk.model.coredata.user.Attribution
import us.frollo.frollosdk.model.coredata.user.Gender
import us.frollo.frollosdk.model.coredata.user.HouseholdType
import us.frollo.frollosdk.model.coredata.user.Industry
import us.frollo.frollosdk.model.coredata.user.Occupation

internal data class UserUpdateRequest(
    @SerializedName("first_name") val firstName: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("primary_currency") val primaryCurrency: String? = null,
    @SerializedName("attribution") val attribution: Attribution? = null,
    @SerializedName("last_name") val lastName: String? = null,
    @SerializedName("mobile_number") val mobileNumber: String? = null,
    @SerializedName("gender") val gender: Gender? = null,
    @SerializedName("address") val currentAddress: Address? = null,
    @SerializedName("household_size") val householdSize: Int? = null,
    @SerializedName("marital_status") val householdType: HouseholdType? = null,
    @SerializedName("occupation") val occupation: Occupation? = null,
    @SerializedName("industry") val industry: Industry? = null,
    @SerializedName("date_of_birth") val dateOfBirth: String? = null, // yyyy-MM or yyyy-MM-dd
    @SerializedName("driver_license") val driverLicense: String? = null
)
