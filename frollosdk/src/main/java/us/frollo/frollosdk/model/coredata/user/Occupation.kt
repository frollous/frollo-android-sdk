package us.frollo.frollosdk.model.coredata.user

import com.google.gson.annotations.SerializedName

enum class Occupation {
    @SerializedName("clerical_and_administrative_workers") CLERICAL_AND_ADMINISTRATIVE_WORKERS,
    @SerializedName("community_and_personal_service_workers") COMMUNITY_AND_PERSONAL_SERVICE_WORKERS,
    @SerializedName("labourers") LABOURERS,
    @SerializedName("machinery_operators_and_drivers") MACHINERY_OPERATORS_AND_DRIVERS,
    @SerializedName("managers") MANAGERS,
    @SerializedName("professionals") PROFESSIONALS,
    @SerializedName("sales_workers") SALES_WORKERS,
    @SerializedName("technicians_and_trades_workers") TECHNICIANS_AND_TRADES_WORKERS
}