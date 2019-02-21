package us.frollo.frollosdk.model

import us.frollo.frollosdk.model.api.aggregation.merchants.MerchantResponse
import us.frollo.frollosdk.model.coredata.aggregation.merchants.MerchantType
import us.frollo.frollosdk.testutils.randomNumber
import us.frollo.frollosdk.testutils.randomUUID
import kotlin.random.Random

internal fun testMerchantResponseData(merchantId: Long? = null) : MerchantResponse {
    return MerchantResponse(
            merchantId = merchantId ?: randomNumber().toLong(),
            name = randomUUID(),
            merchantType = MerchantType.values()[Random.nextInt(MerchantType.values().size)],
            smallLogoUrl = "https://example.com/category.png")
}