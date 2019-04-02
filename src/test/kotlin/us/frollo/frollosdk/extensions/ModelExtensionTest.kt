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

package us.frollo.frollosdk.extensions

import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test
import us.frollo.frollosdk.mapping.toUser
import us.frollo.frollosdk.model.coredata.shared.BudgetCategory
import us.frollo.frollosdk.model.testUserResponseData

class ModelExtensionTest {

    @Test
    fun testUserUpdateRequest() {
        val user = testUserResponseData().toUser()
        val request = user.updateRequest()
        Assert.assertEquals(user.firstName, request.firstName)
    }

    @Test
    fun testGenerateSQLQueryMessages() {
        val query = generateSQLQueryMessages(mutableListOf("survey", "event"), false)
        Assert.assertEquals("SELECT * FROM message WHERE ((message_types LIKE '%|survey|%') OR (message_types LIKE '%|event|%')) AND read = 0", query.sql)
    }

    @Test
    fun testSQLForTransactionStaleIds() {
        val query = sqlForTransactionStaleIds(fromDate = "2019-01-03", toDate = "2019-02-03", accountIds = longArrayOf(123,456), transactionIncluded = false)
        Assert.assertEquals("SELECT transaction_id FROM transaction_model WHERE ((transaction_date BETWEEN Date('2019-01-03') AND Date('2019-02-03'))  AND account_id IN (123,456)  AND included = 0 )", query.sql)
    }

    @Test
    fun testStringToBudgetCategory() {
        var category = "living".toBudgetCategory()
        assertEquals(BudgetCategory.LIVING, category)
        category = "lifestyle".toBudgetCategory()
        assertEquals(BudgetCategory.LIFESTYLE, category)
        category = "income".toBudgetCategory()
        assertEquals(BudgetCategory.INCOME, category)
        category = "goals".toBudgetCategory()
        assertEquals(BudgetCategory.SAVINGS, category)
        category = "one_off".toBudgetCategory()
        assertEquals(BudgetCategory.ONE_OFF, category)
        category = "invalid_category".toBudgetCategory()
        assertEquals(null, category)
    }
}