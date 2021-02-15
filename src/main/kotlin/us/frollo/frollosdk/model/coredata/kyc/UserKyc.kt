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
import us.frollo.frollosdk.model.coredata.user.Address

internal data class UserKyc(

    /** List of addresses */
    @SerializedName("addresses") var addresses: List<Address>?,

    /** Date of birth */
    @SerializedName("date_of_birth") var dateOfBirth: DateOfBirth?,

    /** Email */
    @SerializedName("email") var email: String,

    /** Gender */
    @SerializedName("gender") var gender: String?,

    /** Mobile number */
    @SerializedName("mobile_number") var mobileNumber: String?,

    /** Name */
    @SerializedName("name") var name: Name?,

    /** List of Identity Documents */
    @SerializedName("identity_docs") var identityDocuments: List<IdentityDocument>?,

    /** KYC status */
    @SerializedName("status") val status: KycStatus?
)
