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

package us.frollo.frollosdk.mapping

import org.junit.Assert.assertEquals
import org.junit.Test
import us.frollo.frollosdk.model.testAccountResponseData
import us.frollo.frollosdk.model.testMerchantResponseData
import us.frollo.frollosdk.model.testProviderAccountResponseData
import us.frollo.frollosdk.model.testProviderResponseData
import us.frollo.frollosdk.model.testTransactionCategoryResponseData
import us.frollo.frollosdk.model.testTransactionResponseData
import us.frollo.frollosdk.model.testTransactionsSummaryResponseData

class AggregationMappingTest {

    @Test
    fun testProviderResponseToProvider() {
        val response = testProviderResponseData(providerId = 12345)
        val model = response.toProvider()
        assertEquals(12345L, model.providerId)
    }

    @Test
    fun testProviderResponseToProviderAccount() {
        val response = testProviderAccountResponseData(providerAccountId = 12345)
        val model = response.toProviderAccount()
        assertEquals(12345L, model.providerAccountId)
    }

    @Test
    fun testAccountResponseToAccount() {
        val response = testAccountResponseData(accountId = 12345)
        val model = response.toAccount()
        assertEquals(12345L, model.accountId)
    }

    @Test
    fun testTransactionResponseToTransaction() {
        val response = testTransactionResponseData(transactionId = 12345)
        val model = response.toTransaction()
        assertEquals(12345L, model.transactionId)
    }

    @Test
    fun testTransactionsSummaryResponseToTransactionsSummary() {
        val response = testTransactionsSummaryResponseData(count = 123)
        val model = response.toTransactionsSummary()
        assertEquals(123L, model.count)
    }

    @Test
    fun testTransactionCategoryResponseToTransactionCategory() {
        val response = testTransactionCategoryResponseData(transactionCategoryId = 12345)
        val model = response.toTransactionCategory()
        assertEquals(12345L, model.transactionCategoryId)
    }

    @Test
    fun testMerchantResponseToTransactionCategory() {
        val response = testMerchantResponseData(merchantId = 12345)
        val model = response.toMerchant()
        assertEquals(12345L, model.merchantId)
    }
}
