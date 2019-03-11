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

package us.frollo.frollosdk.model.coredata.user

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.extensions.serializedName

/** Represents occupation of the user */
enum class Occupation {
    /** Clerical and Administrative Workers */
    @SerializedName("clerical_and_administrative_workers") CLERICAL_AND_ADMINISTRATIVE_WORKERS,
    /** Community and Personal Service Workers */
    @SerializedName("community_and_personal_service_workers") COMMUNITY_AND_PERSONAL_SERVICE_WORKERS,
    /** Labourers */
    @SerializedName("labourers") LABOURERS,
    /** Machinery Operators and Drivers */
    @SerializedName("machinery_operators_and_drivers") MACHINERY_OPERATORS_AND_DRIVERS,
    /** Managers */
    @SerializedName("managers") MANAGERS,
    /** Professionals */
    @SerializedName("professionals") PROFESSIONALS,
    /** Sales Workers */
    @SerializedName("sales_workers") SALES_WORKERS,
    /** Technicians and Trades Workers */
    @SerializedName("technicians_and_trades_workers") TECHNICIANS_AND_TRADES_WORKERS;

    /** Enum to serialized string */
    //This override MUST be used for this enum to work with Retrofit @Path or @Query parameters
    override fun toString(): String =
    //Try to get the annotation value if available instead of using plain .toString()
    //Fallback to super.toString() in case annotation is not present/available
            serializedName() ?: super.toString()
}