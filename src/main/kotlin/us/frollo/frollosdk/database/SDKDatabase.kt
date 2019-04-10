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

package us.frollo.frollosdk.database

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import us.frollo.frollosdk.database.dao.*
import us.frollo.frollosdk.model.api.messages.MessageResponse
import us.frollo.frollosdk.model.coredata.aggregation.accounts.Account
import us.frollo.frollosdk.model.coredata.aggregation.merchants.Merchant
import us.frollo.frollosdk.model.coredata.aggregation.provideraccounts.ProviderAccount
import us.frollo.frollosdk.model.coredata.aggregation.providers.Provider
import us.frollo.frollosdk.model.coredata.aggregation.transactioncategories.TransactionCategory
import us.frollo.frollosdk.model.coredata.aggregation.transactions.Transaction
import us.frollo.frollosdk.model.coredata.bills.Bill
import us.frollo.frollosdk.model.coredata.bills.BillPayment
import us.frollo.frollosdk.model.coredata.reports.ReportAccountBalance
import us.frollo.frollosdk.model.coredata.reports.ReportGroupTransactionHistory
import us.frollo.frollosdk.model.coredata.reports.ReportTransactionCurrent
import us.frollo.frollosdk.model.coredata.reports.ReportTransactionHistory
import us.frollo.frollosdk.model.coredata.user.User

@Database(entities = [
    User::class,
    MessageResponse::class,
    Provider::class,
    ProviderAccount::class,
    Account::class,
    Transaction::class,
    TransactionCategory::class,
    Merchant::class,
    ReportTransactionCurrent::class,
    ReportTransactionHistory::class,
    ReportGroupTransactionHistory::class,
    ReportAccountBalance::class,
    Bill::class,
    BillPayment::class
], version = 2, exportSchema = true)

@TypeConverters(Converters::class)
abstract class SDKDatabase : RoomDatabase() {

    internal abstract fun users(): UserDao
    internal abstract fun messages(): MessageDao
    internal abstract fun providers(): ProviderDao
    internal abstract fun providerAccounts(): ProviderAccountDao
    internal abstract fun accounts(): AccountDao
    internal abstract fun transactions(): TransactionDao
    internal abstract fun transactionCategories(): TransactionCategoryDao
    internal abstract fun merchants(): MerchantDao
    internal abstract fun reportsTransactionCurrent(): ReportTransactionCurrentDao
    internal abstract fun reportsTransactionHistory(): ReportTransactionHistoryDao
    internal abstract fun reportGroupsTransactionHistory(): ReportGroupTransactionHistoryDao
    internal abstract fun reportsAccountBalance(): ReportAccountBalanceDao
    internal abstract fun bills(): BillDao
    internal abstract fun billPayments(): BillPaymentDao

    companion object {
        private const val DATABASE_NAME = "frollosdk-db"

        @Volatile private var instance: SDKDatabase? = null // Singleton instantiation

        internal fun getInstance(app: Application): SDKDatabase {
            return instance ?: synchronized(this) {
                instance ?: create(app).also { instance = it }
            }
        }

        private fun create(app: Application): SDKDatabase =
                Room.databaseBuilder(app, SDKDatabase::class.java, DATABASE_NAME)
                        .allowMainThreadQueries() // Needed for some tests
                        //.fallbackToDestructiveMigration()
                        .addMigrations(MIGRATION_1_2)
                        .build()

        // Copy-paste of auto-generated SQLs from room schema json file
        // located in sandbox code after building under frollo-android-sdk/schemas/$version.json
        private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // New changes in this migration:
                // 1) Add new table/entity report_transaction_current and create indexes
                // 2) Add new table/entity report_transaction_history and create indexes
                // 3) Add new table/entity report_group_transaction_history and create indexes
                // 4) Add new table/entity report_account_balance and create indexes

                database.execSQL("CREATE TABLE IF NOT EXISTS `report_transaction_current` (`report_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `day` INTEGER NOT NULL, `linked_id` INTEGER, `linked_name` TEXT, `spend_value` TEXT, `previous_period_value` TEXT, `average_value` TEXT, `budget_value` TEXT, `filtered_budget_category` TEXT, `report_grouping` TEXT NOT NULL)")
                database.execSQL("CREATE INDEX `index_report_transaction_current_report_id` ON `report_transaction_current` (`report_id`)")
                database.execSQL("CREATE UNIQUE INDEX `index_report_transaction_current_linked_id_day_filtered_budget_category_report_grouping` ON `report_transaction_current` (`linked_id`, `day`, `filtered_budget_category`, `report_grouping`)")

                database.execSQL("CREATE TABLE IF NOT EXISTS `report_transaction_history` (`report_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `date` TEXT NOT NULL, `value` TEXT NOT NULL, `budget` TEXT, `period` TEXT NOT NULL, `filtered_budget_category` TEXT, `report_grouping` TEXT NOT NULL)")
                database.execSQL("CREATE INDEX `index_report_transaction_history_report_id` ON `report_transaction_history` (`report_id`)")
                database.execSQL("CREATE UNIQUE INDEX `index_report_transaction_history_date_period_filtered_budget_category_report_grouping` ON `report_transaction_history` (`date`, `period`, `filtered_budget_category`, `report_grouping`)")

                database.execSQL("CREATE TABLE IF NOT EXISTS `report_group_transaction_history` (`report_group_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `linked_id` INTEGER NOT NULL, `linked_name` TEXT NOT NULL, `value` TEXT NOT NULL, `budget` TEXT, `date` TEXT NOT NULL, `period` TEXT NOT NULL, `filtered_budget_category` TEXT, `report_grouping` TEXT NOT NULL, `transaction_ids` TEXT, `report_id` INTEGER NOT NULL)")
                database.execSQL("CREATE INDEX `index_report_group_transaction_history_report_group_id` ON `report_group_transaction_history` (`report_group_id`)")
                database.execSQL("CREATE UNIQUE INDEX `index_report_group_transaction_history_linked_id_date_period_filtered_budget_category_report_grouping` ON `report_group_transaction_history` (`linked_id`, `date`, `period`, `filtered_budget_category`, `report_grouping`)")

                database.execSQL("CREATE TABLE IF NOT EXISTS `report_account_balance` (`report_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `date` TEXT NOT NULL, `account_id` INTEGER NOT NULL, `currency` TEXT NOT NULL, `value` TEXT NOT NULL, `period` TEXT NOT NULL)")
                database.execSQL("CREATE INDEX `index_report_account_balance_report_id` ON `report_account_balance` (`report_id`)")
                database.execSQL("CREATE UNIQUE INDEX `index_report_account_balance_account_id_date_period` ON `report_account_balance` (`account_id`, `date`, `period`)")
            }
        }
    }
}