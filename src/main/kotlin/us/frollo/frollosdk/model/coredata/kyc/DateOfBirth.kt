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

/** Data representation of Date of Birth object */
data class DateOfBirth(

    /** Date of birth in yyyy-MM-dd format */
    @SerializedName("date_of_birth") var dateOfBirth: String?, // yyyy-MM-dd

    /** Year of birth or “unknown”. This will be auto-extracted if dateOfBirth is supplied. */
    @SerializedName("year_of_birth") var yearOfBirth: String?
) {
    companion object {
        /** Date format for date associated with DateOfBirth */
        const val DATE_FORMAT_PATTERN = "yyyy-MM-dd"
    }
}
