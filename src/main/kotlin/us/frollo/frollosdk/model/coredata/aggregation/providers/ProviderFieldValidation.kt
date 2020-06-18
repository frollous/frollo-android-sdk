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

/** Represents a regular expression and associated error to be performed on a field. */
data class ProviderFieldValidation(

    /** Regular expression to be evaluated on the field value */
    @SerializedName("regExp") val regExp: String,

    /** Error message to be displayed if the regex doesn't match */
    @SerializedName("errorMsg") val errorMsg: String
)
