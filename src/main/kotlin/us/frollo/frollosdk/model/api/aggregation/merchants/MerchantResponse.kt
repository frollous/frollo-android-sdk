package us.frollo.frollosdk.model.api.aggregation.merchants

import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.model.coredata.aggregation.merchants.MerchantType

internal data class MerchantResponse(
        @SerializedName("id") val merchantId: Long,
        @SerializedName("name") val name: String,
        @SerializedName("merchant_type") val merchantType: MerchantType,
        @SerializedName("small_logo_url") val smallLogoUrl: String
)