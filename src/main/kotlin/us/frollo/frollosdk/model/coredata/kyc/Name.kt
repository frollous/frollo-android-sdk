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

/** Data representation of name object */
data class Name(

    /**
     * In some cases, the name will need to be supplied in “long form” such as when it is
     * determined from a document scan, or is un-parsable in some way.
     * The service will attempt to convert it to it’s constituent parts where possible
     */
    @SerializedName("display_name") var displayName: String?,

    /** Family name / Surname of the individual (Optional) */
    @SerializedName("family_name") var familyName: String?,

    /** First / Given name (Optional) */
    @SerializedName("given_name") var givenName: String?,

    /** Mr/Ms/Dr/Dame/Dato/etc (Optional) */
    @SerializedName("honourific") var honourific: String?,

    /** Middle name(s) / Initials (Optional) */
    @SerializedName("middle_name") var middleName: String?
)
