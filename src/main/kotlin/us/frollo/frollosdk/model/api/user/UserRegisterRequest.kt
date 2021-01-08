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

internal data class UserRegisterRequest(
    @SerializedName("email") val email: String,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("password") val password: String,

    @SerializedName("address") var address: Address? = null,
    @SerializedName("date_of_birth") var dateOfBirth: String? = null, // yyyy-MM or yyyy-MM-dd
    @SerializedName("last_name") val lastName: String? = null,
    @SerializedName("mobile_number") val mobileNumber: String? = null,
    @SerializedName("client_id") val clientId: String
)
