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

/**
 * Row representing one piece of information to be collected. May have multiple fields which each have their own validation.
 * For example a multiple may have fields for BSB and Account Number.
 * Multiple rows may have the same [fieldRowChoice] indication the user should select one row and fill that in.
 */
data class ProviderFormRow(

    /** Unique ID of the current row */
    @SerializedName("id") val rowId: String,

    /** Label of the row to be displayed to the user */
    @SerializedName("label") val label: String,

    /** Form name */
    @SerializedName("form") val form: String,

    /** Field row choice indicating if this should be grouped with another row or not */
    @SerializedName("fieldRowChoice") val fieldRowChoice: String,

    /** A hint message to be displayed to the user (optional) */
    @SerializedName("hint") val hint: String?,

    /** List of fields for the row */
    @SerializedName("field") val fields: List<ProviderFormField>
)