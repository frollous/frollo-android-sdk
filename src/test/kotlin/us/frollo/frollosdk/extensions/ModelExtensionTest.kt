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
import us.frollo.frollosdk.model.coredata.aggregation.transactions.TransactionFilter
import us.frollo.frollosdk.model.coredata.aggregation.transactions.TransactionStatus
import us.frollo.frollosdk.model.coredata.bills.BillFrequency
import us.frollo.frollosdk.model.coredata.bills.BillPaymentStatus
import us.frollo.frollosdk.model.coredata.bills.BillStatus
import us.frollo.frollosdk.model.coredata.bills.BillType
import us.frollo.frollosdk.model.coredata.budgets.BudgetFrequency
import us.frollo.frollosdk.model.coredata.budgets.BudgetStatus
import us.frollo.frollosdk.model.coredata.budgets.BudgetTrackingStatus
import us.frollo.frollosdk.model.coredata.budgets.BudgetType
import us.frollo.frollosdk.model.coredata.goals.GoalFrequency
import us.frollo.frollosdk.model.coredata.goals.GoalStatus
import us.frollo.frollosdk.model.coredata.goals.GoalTarget
import us.frollo.frollosdk.model.coredata.goals.GoalTrackingStatus
import us.frollo.frollosdk.model.coredata.goals.GoalTrackingType
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
        assertEquals("SELECT COUNT(msg_id)  FROM message WHERE ((message_types LIKE '%|survey|%') OR (message_types LIKE '%|event|%')) AND read = 0 AND content_type = 'VIDEO' ", query.sql)

        query = sqlForMessagesCount()
        assertEquals("SELECT COUNT(msg_id)  FROM message", query.sql)
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
        var query = sqlForProviderAccounts(providerId = 123, refreshStatus = AccountRefreshStatus.ADDING, externalId = "208")
        assertEquals("SELECT  *  FROM provider_account WHERE provider_id = 123 AND r_status_status = 'ADDING' AND external_id = '208' ", query.sql)

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
            refreshStatus = AccountRefreshStatus.NEEDS_ACTION,
            externalId = "208"
        )
        assertEquals("SELECT  *  FROM account WHERE provider_account_id = 123 AND account_status = 'ACTIVE' AND attr_account_sub_type = 'BANK_ACCOUNT' AND attr_account_type = 'BANK' AND attr_account_classification = 'CORPORATE' AND favourite = 1 AND hidden = 0 AND included = 1 AND r_status_status = 'NEEDS_ACTION' AND external_id = '208' ", query.sql)

        query = sqlForAccounts()
        assertEquals("SELECT  *  FROM account", query.sql)
    }

    @Test
    fun testSQLForUpdateAccount() {
        var query = sqlForUpdateAccount(
            accountId = 123,
            hidden = false,
            included = true,
            favourite = true,
            accountSubType = AccountSubType.BANK_ACCOUNT,
            nickName = "CBA"
        )
        assertEquals("UPDATE account SET hidden = 0 , included = 1 , favourite = 1 , attr_account_sub_type = 'BANK_ACCOUNT' , nick_name = 'CBA' WHERE account_id = 123 ", query.sql)

        query = sqlForUpdateAccount(
            accountId = 123,
            hidden = false,
            included = true
        )
        assertEquals("UPDATE account SET hidden = 0 , included = 1 WHERE account_id = 123 ", query.sql)
    }

    @Test
    fun testSQLForTransactionStaleIds() {
        val filter = TransactionFilter(
            transactionIds = listOf(100, 101),
            accountIds = listOf(200, 201),
            transactionCategoryIds = listOf(300, 301),
            merchantIds = listOf(400, 401),
            budgetCategory = BudgetCategory.INCOME,
            searchTerm = "Transfer",
            minimumAmount = "50.78",
            maximumAmount = "150.64",
            baseType = TransactionBaseType.CREDIT,
            tags = listOf("pub", "holiday", "shopping"),
            status = TransactionStatus.PENDING,
            fromDate = "2019-03-01",
            toDate = "2019-03-31",
            transactionIncluded = false,
            accountIncluded = true,
            after = "1577647183_278049",
            before = "1577647183_278048",
            size = 200
        )

        // Days between after & before is N
        var query = sqlForTransactionIdsToGetStaleIds(
            beforeDateString = "2019-01-01",
            afterDateString = "2019-02-01",
            beforeId = 200,
            afterId = 300,
            transactionFilter = filter
        )
        assertEquals("SELECT t.transaction_id  FROM transaction_model AS t LEFT JOIN account a ON t.account_id = a.account_id  WHERE (t.transaction_date <= '2018-12-31' OR (t.transaction_date = '2019-01-01' AND t.transaction_id <= 200)) AND (t.transaction_date >= '2019-02-02' OR (t.transaction_date = '2019-02-01' AND t.transaction_id >= 300)) AND t.transaction_id IN (100,101) AND t.account_id IN (200,201) AND t.merchant_id IN (400,401) AND t.category_id IN (300,301) AND t.budget_category = 'INCOME' AND t.base_type = 'CREDIT' AND t.status = 'PENDING' AND ABS(CAST(t.amount_amount AS DECIMAL)) >= 50.78 AND ABS(CAST(t.amount_amount AS DECIMAL)) <= 150.64 AND t.included = 0 AND a.included = 1 AND  ( t.description_original LIKE '%Transfer%' OR t.description_user LIKE '%Transfer%' OR t.description_simple LIKE '%Transfer%' )  AND ((t.user_tags LIKE '%|pub|%') OR (t.user_tags LIKE '%|holiday|%') OR (t.user_tags LIKE '%|shopping|%')) AND (t.transaction_date BETWEEN Date('2019-03-01') AND Date('2019-03-31')) ", query.sql)

        // First page
        query = sqlForTransactionIdsToGetStaleIds(
            afterDateString = "2019-01-01",
            afterId = 300,
            transactionFilter = filter
        )
        assertEquals("SELECT t.transaction_id  FROM transaction_model AS t LEFT JOIN account a ON t.account_id = a.account_id  WHERE (t.transaction_date >= '2019-01-02' OR (t.transaction_date = '2019-01-01' AND t.transaction_id >= 300)) AND t.transaction_id IN (100,101) AND t.account_id IN (200,201) AND t.merchant_id IN (400,401) AND t.category_id IN (300,301) AND t.budget_category = 'INCOME' AND t.base_type = 'CREDIT' AND t.status = 'PENDING' AND ABS(CAST(t.amount_amount AS DECIMAL)) >= 50.78 AND ABS(CAST(t.amount_amount AS DECIMAL)) <= 150.64 AND t.included = 0 AND a.included = 1 AND  ( t.description_original LIKE '%Transfer%' OR t.description_user LIKE '%Transfer%' OR t.description_simple LIKE '%Transfer%' )  AND ((t.user_tags LIKE '%|pub|%') OR (t.user_tags LIKE '%|holiday|%') OR (t.user_tags LIKE '%|shopping|%')) AND (t.transaction_date BETWEEN Date('2019-03-01') AND Date('2019-03-31')) ", query.sql)

        // Last page
        query = sqlForTransactionIdsToGetStaleIds(
            beforeDateString = "2019-02-01",
            beforeId = 200,
            transactionFilter = filter
        )
        assertEquals("SELECT t.transaction_id  FROM transaction_model AS t LEFT JOIN account a ON t.account_id = a.account_id  WHERE (t.transaction_date <= '2019-01-31' OR (t.transaction_date = '2019-02-01' AND t.transaction_id <= 200)) AND t.transaction_id IN (100,101) AND t.account_id IN (200,201) AND t.merchant_id IN (400,401) AND t.category_id IN (300,301) AND t.budget_category = 'INCOME' AND t.base_type = 'CREDIT' AND t.status = 'PENDING' AND ABS(CAST(t.amount_amount AS DECIMAL)) >= 50.78 AND ABS(CAST(t.amount_amount AS DECIMAL)) <= 150.64 AND t.included = 0 AND a.included = 1 AND  ( t.description_original LIKE '%Transfer%' OR t.description_user LIKE '%Transfer%' OR t.description_simple LIKE '%Transfer%' )  AND ((t.user_tags LIKE '%|pub|%') OR (t.user_tags LIKE '%|holiday|%') OR (t.user_tags LIKE '%|shopping|%')) AND (t.transaction_date BETWEEN Date('2019-03-01') AND Date('2019-03-31')) ", query.sql)

        // Single page with filters
        query = sqlForTransactionIdsToGetStaleIds(
            transactionFilter = filter
        )
        assertEquals("SELECT t.transaction_id  FROM transaction_model AS t LEFT JOIN account a ON t.account_id = a.account_id  WHERE t.transaction_id IN (100,101) AND t.account_id IN (200,201) AND t.merchant_id IN (400,401) AND t.category_id IN (300,301) AND t.budget_category = 'INCOME' AND t.base_type = 'CREDIT' AND t.status = 'PENDING' AND ABS(CAST(t.amount_amount AS DECIMAL)) >= 50.78 AND ABS(CAST(t.amount_amount AS DECIMAL)) <= 150.64 AND t.included = 0 AND a.included = 1 AND  ( t.description_original LIKE '%Transfer%' OR t.description_user LIKE '%Transfer%' OR t.description_simple LIKE '%Transfer%' )  AND ((t.user_tags LIKE '%|pub|%') OR (t.user_tags LIKE '%|holiday|%') OR (t.user_tags LIKE '%|shopping|%')) AND (t.transaction_date BETWEEN Date('2019-03-01') AND Date('2019-03-31')) ", query.sql)

        // Single page, No filters
        query = sqlForTransactionIdsToGetStaleIds()
        assertEquals("SELECT t.transaction_id  FROM transaction_model AS t LEFT JOIN account a ON t.account_id = a.account_id ", query.sql)
    }

    @Test
    fun testSQLForTransactions() {
        val filter = TransactionFilter(
            transactionIds = listOf(100, 101),
            accountIds = listOf(200, 201),
            transactionCategoryIds = listOf(300, 301),
            merchantIds = listOf(400, 401),
            budgetCategory = BudgetCategory.INCOME,
            searchTerm = "Transfer",
            minimumAmount = "50.78",
            maximumAmount = "150.64",
            baseType = TransactionBaseType.CREDIT,
            tags = listOf("pub", "holiday", "shopping"),
            status = TransactionStatus.PENDING,
            fromDate = "2019-03-01",
            toDate = "2019-03-31",
            transactionIncluded = false,
            accountIncluded = true,
            after = "1577647183_278049",
            before = "1577647183_278048",
            size = 200
        )

        var query = sqlForTransactions(filter)
        assertEquals("SELECT t.*  FROM transaction_model AS t LEFT JOIN account a ON t.account_id = a.account_id  WHERE t.transaction_id IN (100,101) AND t.account_id IN (200,201) AND t.merchant_id IN (400,401) AND t.category_id IN (300,301) AND t.budget_category = 'INCOME' AND t.base_type = 'CREDIT' AND t.status = 'PENDING' AND ABS(CAST(t.amount_amount AS DECIMAL)) >= 50.78 AND ABS(CAST(t.amount_amount AS DECIMAL)) <= 150.64 AND t.included = 0 AND a.included = 1 AND  ( t.description_original LIKE '%Transfer%' OR t.description_user LIKE '%Transfer%' OR t.description_simple LIKE '%Transfer%' )  AND ((t.user_tags LIKE '%|pub|%') OR (t.user_tags LIKE '%|holiday|%') OR (t.user_tags LIKE '%|shopping|%')) AND (t.transaction_date BETWEEN Date('2019-03-01') AND Date('2019-03-31')) ", query.sql)

        query = sqlForTransactions()
        assertEquals("SELECT t.*  FROM transaction_model AS t LEFT JOIN account a ON t.account_id = a.account_id ", query.sql)
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
    fun testSQLForMerchantIds() {
        var query = sqlForMerchantsIds(before = 100, after = 200)
        assertEquals("SELECT merchant_id  FROM merchant WHERE merchant_id > 100 AND merchant_id <= 200 ", query.sql)

        query = sqlForMerchantsIds()
        assertEquals("SELECT merchant_id  FROM merchant", query.sql)
    }

    @Test
    fun testSQLForGoals() {
        var query = sqlForGoals(frequency = GoalFrequency.MONTHLY, trackingStatus = GoalTrackingStatus.EQUAL, status = GoalStatus.ACTIVE, accountId = 12345, trackingType = GoalTrackingType.CREDIT, target = GoalTarget.OPEN_ENDED)
        assertEquals("SELECT  *  FROM goal WHERE frequency = 'MONTHLY' AND status = 'ACTIVE' AND target = 'OPEN_ENDED' AND tracking_status = 'EQUAL' AND tracking_type = 'CREDIT' AND account_id = 12345 ", query.sql)

        query = sqlForGoals()
        assertEquals("SELECT  *  FROM goal", query.sql)
    }

    @Test
    fun testSQLForGoalIds() {
        var query = sqlForGoalIds(trackingStatus = GoalTrackingStatus.EQUAL, status = GoalStatus.ACTIVE)
        assertEquals("SELECT goal_id  FROM goal WHERE status = 'ACTIVE' AND tracking_status = 'EQUAL' ", query.sql)

        query = sqlForGoalIds()
        assertEquals("SELECT goal_id  FROM goal", query.sql)
    }

    @Test
    fun testSQLForGoalPeriods() {
        var query = sqlForGoalPeriods(goalId = 12345, trackingStatus = GoalTrackingStatus.EQUAL)
        assertEquals("SELECT  *  FROM goal_period WHERE goal_id = 12345 AND tracking_status = 'EQUAL' ", query.sql)

        query = sqlForGoalPeriods()
        assertEquals("SELECT  *  FROM goal_period", query.sql)
    }

    @Test
    fun testSQLForBudgets() {
        var query = sqlForBudgets(true, BudgetFrequency.MONTHLY, BudgetStatus.UNSTARTED, BudgetTrackingStatus.BELOW, BudgetType.MERCHANT, "1")
        assertEquals("SELECT  *  FROM budget WHERE frequency = 'MONTHLY' AND status = 'UNSTARTED' AND tracking_status = 'BELOW' AND type = 'MERCHANT' AND type_value = '1' AND is_current = 1 ", query.sql)

        query = sqlForBudgets()
        assertEquals("SELECT  *  FROM budget", query.sql)
    }

    @Test
    fun testSQLForBudgetIds() {
        var query = sqlForBudgetIds(true, BudgetType.TRANSACTION_CATEGORY)
        assertEquals("SELECT budget_id  FROM budget WHERE is_current = 1 AND type = 'TRANSACTION_CATEGORY' ", query.sql)

        query = sqlForBudgetIds()
        assertEquals("SELECT budget_id  FROM budget", query.sql)
    }

    @Test
    fun testSQLForBudgetPeriodIds() {
        var query = sqlForBudgetPeriodIds(123, "2019-11-20", "2019-11-23")
        assertEquals("SELECT budget_period_id  FROM budget_period WHERE budget_id = 123  AND (start_date BETWEEN Date('2019-11-20') AND Date('2019-11-23')) ", query.sql)

        query = sqlForBudgetPeriodIds(123)
        assertEquals("SELECT budget_period_id  FROM budget_period WHERE budget_id = 123  ", query.sql)
    }

    @Test
    fun testSQLForBudgetPeriod() {
        var query = sqlForBudgetPeriods(123, BudgetTrackingStatus.EQUAL)
        assertEquals("SELECT  *  FROM budget_period WHERE budget_id = 123 AND tracking_status = 'EQUAL' ", query.sql)

        query = sqlForBudgetPeriods(123)
        assertEquals("SELECT  *  FROM budget_period WHERE budget_id = 123 ", query.sql)
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
