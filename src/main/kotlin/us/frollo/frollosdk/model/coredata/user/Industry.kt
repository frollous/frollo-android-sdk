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

/**
 * Represents the industry the user works in
 */
enum class Industry {
    /** Accommodation and Food Services */
    @SerializedName("accommodation_and_food_services") ACCOMMODATION_AND_FOOD_SERVICES,
    /** Administrative and Support Services */
    @SerializedName("administrative_and_support_services") ADMINISTRATIVE_AND_SUPPORT_SERVICES,
    /** Arts and Recreation Services */
    @SerializedName("arts_and_recreations_services") ARTS_AND_RECREATIONS_SERVICES,
    /** Construction */
    @SerializedName("construction") CONSTRUCTION,
    /** Education and Training */
    @SerializedName("education_and_training") EDUCATION_AND_TRAINING,
    /** Electricity, Gas, Water and Waste Services */
    @SerializedName("electricity_gas_water_and_waste_services") ELECTRICITY_GAS_WATER_AND_WASTE_SERVICES,
    /** Financial and Insurance Services */
    @SerializedName("financial_and_insurance_services") FINANCIAL_AND_INSURANCE_SERVICES,
    /** Healthcare and Social Assistance */
    @SerializedName("health_care_and_social_assistance") HEALTH_CARE_AND_SOCIAL_ASSISTANCE,
    /** Information, Media and Telecommunications */
    @SerializedName("information_media_and_telecommunications") INFORMATION_MEDIA_AND_TELECOMMUNICATIONS,
    /** Manufacturing */
    @SerializedName("manufacturing") MANUFACTURING,
    /** Mining */
    @SerializedName("mining") MINING,
    /** Other Services */
    @SerializedName("other_services") OTHER_SERVICES,
    /** Professional, Scientific and Technical Services */
    @SerializedName("professional_scientific_and_technical_services") PROFESSIONAL_SCIENTIFIC_AND_TECHNICAL_SERVICES,
    /** Public Administration and Safety */
    @SerializedName("public_administration_and_safety") PUBLIC_ADMINISTRATION_AND_SAFETY,
    /** Rental, Hiring and Real Estate Services */
    @SerializedName("rental_hiring_and_real_estate_services") RENTAL_HIRING_AND_REAL_ESTATE_SERVICES,
    /** Retail Trade */
    @SerializedName("retail_trade") RETAIL_TRADE,
    /** Transport, Postal and Warehousing */
    @SerializedName("transport_postal_and_warehousing") TRANSPORT_POSTAL_AND_WAREHOUSING,
    /** Wholesale Trade */
    @SerializedName("wholesale_trade") WHOLESALE_TRADE;

    /** Enum to serialized string */
    // This override MUST be used for this enum to work with Retrofit @Path or @Query parameters
    override fun toString(): String =
        // Try to get the annotation value if available instead of using plain .toString()
        // Fallback to super.toString() in case annotation is not present/available
        serializedName() ?: super.toString()
}
