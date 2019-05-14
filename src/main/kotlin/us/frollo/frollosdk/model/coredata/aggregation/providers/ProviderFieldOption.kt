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

/** Details for display of an option if the field contains a list of options */
data class ProviderFieldOption(

    /** Text to be displayed to the user */
    @SerializedName("displayText") val displayText: String,

    /** Selected indicator. Updated when a user selects an option (optional) */
    @SerializedName("optionValue") val optionValue: String,

    /** Value of the option */
    @SerializedName("isSelected") var isSelected: Boolean?
) {

    /** String representation of the object. Useful if the field is a Spinner. */
    override fun toString(): String = displayText
}