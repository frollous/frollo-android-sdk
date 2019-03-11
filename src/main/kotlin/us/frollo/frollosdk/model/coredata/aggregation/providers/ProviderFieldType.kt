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
import us.frollo.frollosdk.extensions.serializedName

/** Type of field indicating what type of data will be provided and how it should be displayed */
enum class ProviderFieldType {

    /** Checkbox. Show a standard check box to the user */
    @SerializedName("checkbox") CHECKBOX,

    /** Image. Show the image to the user */
    @SerializedName("image") IMAGE,

    /** Option. Show a drop down list of options to the user */
    @SerializedName("option") OPTION,

    /** Password. Show a secure text field to the user */
    @SerializedName("password") PASSWORD,

    /** Radio button. Show a radio button list to the user */
    @SerializedName("radio") RADIO,

    /** Text. Show a regular text field to the user */
    @SerializedName("text") TEXT;

    /** Enum to serialized string */
    //This override MUST be used for this enum to work with Retrofit @Path or @Query parameters
    override fun toString(): String =
    //Try to get the annotation value if available instead of using plain .toString()
    //Fallback to super.toString() in case annotation is not present/available
            serializedName() ?: super.toString()
}