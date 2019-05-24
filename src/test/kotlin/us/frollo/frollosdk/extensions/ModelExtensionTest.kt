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

import org.junit.Assert.assertEquals
import org.junit.Test
import us.frollo.frollosdk.mapping.toUser
import us.frollo.frollosdk.model.coredata.aggregation.accounts.AccountClassification
import us.frollo.frollosdk.model.coredata.aggregation.accounts.AccountStatus
import us.frollo.frollosdk.model.coredata.aggregation.accounts.AccountSubType
import us.frollo.frollosdk.model.coredata.aggregation.accounts.AccountType
import us.frollo.frollosdk.model.coredata.aggregation.merchants.MerchantType
import us.frollo.frollosdk.model.coredata.aggregation.provideraccounts.AccountRefreshStatus
import us.frollo.frollosdk.model.coredata.aggregation.providers.ProviderStatus
import us.frollo.frollosdk.model.coredata.aggregation.tags.TagsSortType
import us.frollo.frollosdk.model.coredata.aggregation.transactioncategories.TransactionCategoryType
import us.frollo.frollosdk.model.coredata.aggregation.transactions.TransactionBaseType
import us.frollo.frollosdk.model.coredata.aggregation.transactions.TransactionStatus
import us.frollo.frollosdk.model.coredata.bills.BillFrequency
import us.frollo.frollosdk.model.coredata.bills.BillPaymentStatus
import us.frollo.frollosdk.model.coredata.bills.BillStatus
import us.frollo.frollosdk.model.coredata.bills.BillType
import us.frollo.frollosdk.model.coredata.messages.ContentType
import us.frollo.frollosdk.model.coredata.reports.ReportPeriod
import us.frollo.frollosdk.model.coredata.shared.BudgetCategory
import us.frollo.frollosdk.model.coredata.shared.OrderType
import us.frollo.frollosdk.model.testUserResponseData

class ModelExtensionTest {

    @Test
    fun testUserUpdateRequest() {
        val user = testUserResponseData().toUser()
        val request = user.updateRequest()
        assertEquals(user.firstName, request.firstName)
    }

    @Test
    fun testSQLForMessages() {
        var query = sqlForMessages(mutableListOf("survey", "event"), false)
        assertEquals("SELECT  *  FROM message WHERE ((message_types LIKE '%|survey|%') OR (message_types LIKE '%|event|%')) AND read = 0 ", query.sql)

        query = sqlForMessages(mutableListOf("survey", "event"), false, ContentType.VIDEO)
        assertEquals("SELECT  *  FROM message WHERE ((message_types LIKE '%|survey|%') OR (message_types LIKE '%|event|%')) AND read = 0 AND content_type = 'VIDEO' ", query.sql)

        query = sqlForMessages(mutableListOf("survey", "event"))
        assertEquals("SELECT  *  FROM message WHERE ((message_types LIKE '%|survey|%') OR (message_types LIKE '%|event|%')) ", query.sql)

        query = sqlForMessages()
        assertEquals("SELECT  *  FROM message", query.sql)
    }

    @Test
    fun testSQLForMessagesCount() {
        var query = sqlForMessagesCount(mutableListOf("survey", "event"), false, ContentType.VIDEO)
        assertEquals("SELECT COUNT(message_id)  FROM message WHERE ((message_types LIKE '%|survey|%') OR (message_types LIKE '%|event|%')) AND read = 0 AND content_type = 'VIDEO' ", query.sql)

        query = sqlForMessagesCount()
        assertEquals("SELECT COUNT(message_id)  FROM message", query.sql)
    }

    @Test
    fun testSQLForTransactionStaleIds() {
        val query = sqlForTransactionStaleIds(fromDate = "2019-01-03", toDate = "2019-02-03", accountIds = longArrayOf(123, 456), transactionIncluded = false)
        assertEquals("SELECT transaction_id FROM transaction_model WHERE ((transaction_date BETWEEN Date('2019-01-03') AND Date('2019-02-03'))  AND account_id IN (123,456)  AND included = 0 )", query.sql)
    }

    @Test
    fun testSQLForExistingAccountBalanceReports() {
        var query = sqlForExistingAccountBalanceReports(date = "2019-01", period = ReportPeriod.MONTH, reportAccountIds = longArrayOf(123, 456), accountId = 123, accountType = AccountType.BANK)
        assertEquals("SELECT rab.* FROM report_account_balance AS rab  LEFT JOIN account AS a ON rab.account_id = a.account_id  WHERE rab.period = 'MONTH' AND rab.date = '2019-01' AND rab.account_id IN (123,456)  AND rab.account_id = 123  AND a.attr_account_type = 'BANK' ", query.sql)

        query = sqlForExistingAccountBalanceReports(date = "2019-01", period = ReportPeriod.MONTH, reportAccountIds = longArrayOf(123, 456), accountId = 123, accountType = null)
        assertEquals("SELECT rab.* FROM report_account_balance AS rab  WHERE rab.period = 'MONTH' AND rab.date = '2019-01' AND rab.account_id IN (123,456)  AND rab.account_id = 123 ", query.sql)

        query = sqlForExistingAccountBalanceReports(date = "2019-01", period = ReportPeriod.MONTH, reportAccountIds = longArrayOf(123, 456), accountId = null, accountType = AccountType.BANK)
        assertEquals("SELECT rab.* FROM report_account_balance AS rab  LEFT JOIN account AS a ON rab.account_id = a.account_id  WHERE rab.period = 'MONTH' AND rab.date = '2019-01' AND rab.account_id IN (123,456)  AND a.attr_account_type = 'BANK' ", query.sql)

        query = sqlForExistingAccountBalanceReports(date = "2019-01", period = ReportPeriod.MONTH, reportAccountIds = longArrayOf(123, 456), accountId = null, accountType = null)
        assertEquals("SELECT rab.* FROM report_account_balance AS rab  WHERE rab.period = 'MONTH' AND rab.date = '2019-01' AND rab.account_id IN (123,456) ", query.sql)
    }

    @Test
    fun testSQLForStaleIdsAccountBalanceReports() {
        var query = sqlForStaleIdsAccountBalanceReports(date = "2019-01", period = ReportPeriod.MONTH, reportAccountIds = longArrayOf(123, 456), accountId = 123, accountType = AccountType.BANK)
        assertEquals("SELECT rab.* FROM report_account_balance AS rab  LEFT JOIN account AS a ON rab.account_id = a.account_id  WHERE rab.period = 'MONTH' AND rab.date = '2019-01' AND rab.account_id NOT IN (123,456)  AND rab.account_id = 123  AND a.attr_account_type = 'BANK' ", query.sql)

        query = sqlForStaleIdsAccountBalanceReports(date = "2019-01", period = ReportPeriod.MONTH, reportAccountIds = longArrayOf(123, 456), accountId = 123, accountType = null)
        assertEquals("SELECT rab.* FROM report_account_balance AS rab  WHERE rab.period = 'MONTH' AND rab.date = '2019-01' AND rab.account_id NOT IN (123,456)  AND rab.account_id = 123 ", query.sql)

        query = sqlForStaleIdsAccountBalanceReports(date = "2019-01", period = ReportPeriod.MONTH, reportAccountIds = longArrayOf(123, 456), accountId = null, accountType = AccountType.BANK)
        assertEquals("SELECT rab.* FROM report_account_balance AS rab  LEFT JOIN account AS a ON rab.account_id = a.account_id  WHERE rab.period = 'MONTH' AND rab.date = '2019-01' AND rab.account_id NOT IN (123,456)  AND a.attr_account_type = 'BANK' ", query.sql)

        query = sqlForStaleIdsAccountBalanceReports(date = "2019-01", period = ReportPeriod.MONTH, reportAccountIds = longArrayOf(123, 456), accountId = null, accountType = null)
        assertEquals("SELECT rab.* FROM report_account_balance AS rab  WHERE rab.period = 'MONTH' AND rab.date = '2019-01' AND rab.account_id NOT IN (123,456) ", query.sql)
    }

    @Test
    fun testSQLForFetchingAccountBalanceReports() {
        var query = sqlForFetchingAccountBalanceReports(fromDate = "2019-01", toDate = "2019-12", period = ReportPeriod.MONTH, accountId = 123, accountType = AccountType.BANK)
        assertEquals("SELECT rab.* FROM report_account_balance AS rab  LEFT JOIN account AS a ON rab.account_id = a.account_id  WHERE rab.period = 'MONTH' AND (rab.date BETWEEN '2019-01' AND '2019-12')  AND rab.account_id = 123  AND a.attr_account_type = 'BANK' ", query.sql)

        query = sqlForFetchingAccountBalanceReports(fromDate = "2019-01", toDate = "2019-12", period = ReportPeriod.MONTH, accountId = 123, accountType = null)
        assertEquals("SELECT rab.* FROM report_account_balance AS rab  WHERE rab.period = 'MONTH' AND (rab.date BETWEEN '2019-01' AND '2019-12')  AND rab.account_id = 123 ", query.sql)

        query = sqlForFetchingAccountBalanceReports(fromDate = "2019-01", toDate = "2019-12", period = ReportPeriod.MONTH, accountId = null, accountType = AccountType.BANK)
        assertEquals("SELECT rab.* FROM report_account_balance AS rab  LEFT JOIN account AS a ON rab.account_id = a.account_id  WHERE rab.period = 'MONTH' AND (rab.date BETWEEN '2019-01' AND '2019-12')  AND a.attr_account_type = 'BANK' ", query.sql)

        query = sqlForFetchingAccountBalanceReports(fromDate = "2019-01", toDate = "2019-12", period = ReportPeriod.MONTH, accountId = null, accountType = null)
        assertEquals("SELECT rab.* FROM report_account_balance AS rab  WHERE rab.period = 'MONTH' AND (rab.date BETWEEN '2019-01' AND '2019-12') ", query.sql)
    }

    @Test
    fun testSQLForUserTags() {
        var query = sqlForUserTags(searchTerm = "pub", sortBy = TagsSortType.CREATED_AT, orderBy = OrderType.ASC)
        assertEquals("SELECT  *  FROM transaction_user_tags WHERE name LIKE '%pub%'  ORDER BY created_at asc", query.sql)

        query = sqlForUserTags()
        assertEquals("SELECT  *  FROM transaction_user_tags ORDER BY name asc", query.sql)
    }

    @Test
    fun testSQLForBills() {
        var query = sqlForBills(frequency = BillFrequency.MONTHLY, paymentStatus = BillPaymentStatus.FUTURE, status = BillStatus.CONFIRMED, type = BillType.MANUAL)
        assertEquals("SELECT  *  FROM bill WHERE frequency = 'MONTHLY' AND payment_status = 'FUTURE' AND status = 'CONFIRMED' AND bill_type = 'MANUAL' ", query.sql)

        query = sqlForBills()
        assertEquals("SELECT  *  FROM bill", query.sql)
    }

    @Test
    fun testSQLForBillPayments() {
        var query = sqlForBillPayments(billId = 123, fromDate = "2019-03-01", toDate = "2019-03-31", frequency = BillFrequency.MONTHLY, paymentStatus = BillPaymentStatus.FUTURE)
        assertEquals("SELECT  *  FROM bill_payment WHERE bill_id = 123 AND (date BETWEEN Date('2019-03-01') AND Date('2019-03-31')) AND frequency = 'MONTHLY' AND payment_status = 'FUTURE' ", query.sql)

        query = sqlForBillPayments()
        assertEquals("SELECT  *  FROM bill_payment", query.sql)
    }

    @Test
    fun testSQLForProviders() {
        var query = sqlForProviders(status = ProviderStatus.SUPPORTED)
        assertEquals("SELECT  *  FROM provider WHERE provider_status = 'SUPPORTED' ", query.sql)

        query = sqlForProviders()
        assertEquals("SELECT  *  FROM provider", query.sql)
    }

    @Test
    fun testSQLForProviderAccounts() {
        var query = sqlForProviderAccounts(providerId = 123, refreshStatus = AccountRefreshStatus.ADDING)
        assertEquals("SELECT  *  FROM provider_account WHERE provider_id = 123 AND r_status_status = 'ADDING' ", query.sql)

        query = sqlForProviderAccounts()
        assertEquals("SELECT  *  FROM provider_account", query.sql)
    }

    @Test
    fun testSQLForAccounts() {
        var query = sqlForAccounts(
                providerAccountId = 123,
                accountStatus = AccountStatus.ACTIVE,
                accountSubType = AccountSubType.BANK_ACCOUNT,
                accountType = AccountType.BANK,
                accountClassification = AccountClassification.CORPORATE,
                favourite = true,
                hidden = false,
                included = true,
                refreshStatus = AccountRefreshStatus.NEEDS_ACTION)
        assertEquals("SELECT  *  FROM account WHERE provider_account_id = 123 AND account_status = 'ACTIVE' AND attr_account_sub_type = 'BANK_ACCOUNT' AND attr_account_type = 'BANK' AND attr_account_classification = 'CORPORATE' AND favourite = 1 AND hidden = 0 AND included = 1 AND r_status_status = 'NEEDS_ACTION' ", query.sql)

        query = sqlForAccounts()
        assertEquals("SELECT  *  FROM account", query.sql)
    }

    @Test
    fun testSQLForTransactions() {
        var query = sqlForTransactions(
                accountId = 123,
                userTags = listOf("pub", "holiday", "shopping"),
                baseType = TransactionBaseType.CREDIT,
                budgetCategory = BudgetCategory.INCOME,
                status = TransactionStatus.PENDING,
                included = false,
                fromDate = "2019-03-01",
                toDate = "2019-03-31")
        assertEquals("SELECT  *  FROM transaction_model WHERE account_id = 123 AND ((user_tags LIKE '%|pub|%') AND (user_tags LIKE '%|holiday|%') AND (user_tags LIKE '%|shopping|%')) AND base_type = 'CREDIT' AND budget_category = 'INCOME' AND status = 'PENDING' AND included = 0 AND (transaction_date BETWEEN Date('2019-03-01') AND Date('2019-03-31')) ", query.sql)

        query = sqlForTransactions()
        assertEquals("SELECT  *  FROM transaction_model", query.sql)
    }

    @Test
    fun testSQLForTransactionCategories() {
        var query = sqlForTransactionCategories(defaultBudgetCategory = BudgetCategory.INCOME, type = TransactionCategoryType.CREDIT_SCORE)
        assertEquals("SELECT  *  FROM transaction_category WHERE default_budget_category = 'INCOME' AND category_type = 'CREDIT_SCORE' ", query.sql)

        query = sqlForTransactionCategories()
        assertEquals("SELECT  *  FROM transaction_category", query.sql)
    }

    @Test
    fun testSQLForMerchants() {
        var query = sqlForMerchants(type = MerchantType.RETAILER)
        assertEquals("SELECT  *  FROM merchant WHERE merchant_type = 'RETAILER' ", query.sql)

        query = sqlForMerchants()
        assertEquals("SELECT  *  FROM merchant", query.sql)
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