package us.frollo.frollosdk.model.coredata.user

import com.google.gson.annotations.SerializedName

enum class Industry {
    @SerializedName("accommodation_and_food_services") ACCOMMODATION_AND_FOOD_SERVICES,
    @SerializedName("administrative_and_support_services") ADMINISTRATIVE_AND_SUPPORT_SERVICES,
    @SerializedName("arts_and_recreations_services") ARTS_AND_RECREATIONS_SERVICES,
    @SerializedName("construction") CONSTRUCTION,
    @SerializedName("education_and_training") EDUCATION_AND_TRAINING,
    @SerializedName("electricity_gas_water_and_waste_services") ELECTRICITY_GAS_WATER_AND_WASTE_SERVICES,
    @SerializedName("financial_and_insurance_services") FINANCIAL_AND_INSURANCE_SERVICES,
    @SerializedName("health_care_and_social_assistance") HEALTH_CARE_AND_SOCIAL_ASSISTANCE,
    @SerializedName("information_media_and_telecommunications") INFORMATION_MEDIA_AND_TELECOMMUNICATIONS,
    @SerializedName("manufacturing") MANUFACTURING,
    @SerializedName("mining") MINING,
    @SerializedName("other_services") OTHER_SERVICES,
    @SerializedName("professional_scientific_and_technical_services") PROFESSIONAL_SCIENTIFIC_AND_TECHNICAL_SERVICES,
    @SerializedName("public_administration_and_safety") PUBLIC_ADMINISTRATION_AND_SAFETY,
    @SerializedName("rental_hiring_and_real_estate_services") RENTAL_HIRING_AND_REAL_ESTATE_SERVICES,
    @SerializedName("retail_trade") RETAIL_TRADE,
    @SerializedName("transport_postal_and_warehousing") TRANSPORT_POSTAL_AND_WAREHOUSING,
    @SerializedName("wholesale_trade") WHOLESALE_TRADE
}