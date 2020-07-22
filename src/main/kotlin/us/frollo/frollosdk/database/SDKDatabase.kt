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
import us.frollo.frollosdk.database.dao.AccountDao
import us.frollo.frollosdk.database.dao.BillDao
import us.frollo.frollosdk.database.dao.BillPaymentDao
import us.frollo.frollosdk.database.dao.BudgetDao
import us.frollo.frollosdk.database.dao.BudgetPeriodDao
import us.frollo.frollosdk.database.dao.GoalDao
import us.frollo.frollosdk.database.dao.GoalPeriodDao
import us.frollo.frollosdk.database.dao.MerchantDao
import us.frollo.frollosdk.database.dao.MessageDao
import us.frollo.frollosdk.database.dao.ProviderAccountDao
import us.frollo.frollosdk.database.dao.ProviderDao
import us.frollo.frollosdk.database.dao.ReportAccountBalanceDao
import us.frollo.frollosdk.database.dao.TransactionCategoryDao
import us.frollo.frollosdk.database.dao.TransactionDao
import us.frollo.frollosdk.database.dao.TransactionUserTagsDao
import us.frollo.frollosdk.database.dao.UserDao
import us.frollo.frollosdk.model.api.messages.MessageResponse
import us.frollo.frollosdk.model.coredata.aggregation.accounts.Account
import us.frollo.frollosdk.model.coredata.aggregation.merchants.Merchant
import us.frollo.frollosdk.model.coredata.aggregation.provideraccounts.ProviderAccount
import us.frollo.frollosdk.model.coredata.aggregation.providers.Provider
import us.frollo.frollosdk.model.coredata.aggregation.tags.TransactionTag
import us.frollo.frollosdk.model.coredata.aggregation.transactioncategories.TransactionCategory
import us.frollo.frollosdk.model.coredata.aggregation.transactions.Transaction
import us.frollo.frollosdk.model.coredata.bills.Bill
import us.frollo.frollosdk.model.coredata.bills.BillPayment
import us.frollo.frollosdk.model.coredata.budgets.Budget
import us.frollo.frollosdk.model.coredata.budgets.BudgetPeriod
import us.frollo.frollosdk.model.coredata.goals.Goal
import us.frollo.frollosdk.model.coredata.goals.GoalPeriod
import us.frollo.frollosdk.model.coredata.reports.ReportAccountBalance
import us.frollo.frollosdk.model.coredata.user.User

@Database(
    entities = [
        User::class,
        MessageResponse::class,
        Provider::class,
        ProviderAccount::class,
        Account::class,
        Transaction::class,
        TransactionCategory::class,
        Merchant::class,
        ReportAccountBalance::class,
        Bill::class,
        BillPayment::class,
        TransactionTag::class,
        Goal::class,
        GoalPeriod::class,
        Budget::class,
        BudgetPeriod::class
    ],
    version = 10, exportSchema = true
)

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
    internal abstract fun reportsAccountBalance(): ReportAccountBalanceDao
    internal abstract fun bills(): BillDao
    internal abstract fun billPayments(): BillPaymentDao
    internal abstract fun userTags(): TransactionUserTagsDao
    internal abstract fun goals(): GoalDao
    internal abstract fun goalPeriods(): GoalPeriodDao
    internal abstract fun budgets(): BudgetDao
    internal abstract fun budgetPeriods(): BudgetPeriodDao

    companion object {
        private const val DATABASE_NAME = "frollosdk-db"

        @Volatile
        private var instance: SDKDatabase? = null // Singleton instantiation

        internal fun getInstance(app: Application): SDKDatabase {
            return instance ?: synchronized(this) {
                instance ?: create(app).also { instance = it }
            }
        }

        private fun create(app: Application): SDKDatabase =
            Room.databaseBuilder(app, SDKDatabase::class.java, DATABASE_NAME)
                .allowMainThreadQueries() // Needed for some tests
                // .fallbackToDestructiveMigration()
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_6_8, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10)
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

        private val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // New changes in this migration:
                // 1) Alter table/entity transaction_model - add columns: "merchant_name", "merchant_phone", "merchant_website", "merchant_location"
                // 2) Add new table/entity bill and create indexes
                // 3) Add new table/entity bill_payment and create indexes
                // 4) Alter table/entity message - add column: "auto_dismiss"

                database.execSQL("ALTER TABLE `transaction_model` ADD COLUMN `merchant_name` TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE `transaction_model` ADD COLUMN `merchant_phone` TEXT")
                database.execSQL("ALTER TABLE `transaction_model` ADD COLUMN `merchant_website` TEXT")
                database.execSQL("ALTER TABLE `transaction_model` ADD COLUMN `merchant_location` TEXT")

                database.execSQL("CREATE TABLE IF NOT EXISTS `bill` (`bill_id` INTEGER NOT NULL, `name` TEXT, `description` TEXT, `bill_type` TEXT NOT NULL, `status` TEXT NOT NULL, `last_amount` TEXT, `due_amount` TEXT NOT NULL, `average_amount` TEXT NOT NULL, `frequency` TEXT NOT NULL, `payment_status` TEXT NOT NULL, `last_payment_date` TEXT, `next_payment_date` TEXT NOT NULL, `category_id` INTEGER, `merchant_id` INTEGER, `account_id` INTEGER, `note` TEXT, PRIMARY KEY(`bill_id`))")
                database.execSQL("CREATE INDEX `index_bill_bill_id` ON `bill` (`bill_id`)")
                database.execSQL("CREATE INDEX `index_bill_merchant_id` ON `bill` (`merchant_id`)")
                database.execSQL("CREATE INDEX `index_bill_category_id` ON `bill` (`category_id`)")
                database.execSQL("CREATE INDEX `index_bill_account_id` ON `bill` (`account_id`)")

                database.execSQL("CREATE TABLE IF NOT EXISTS `bill_payment` (`bill_payment_id` INTEGER NOT NULL, `bill_id` INTEGER NOT NULL, `name` TEXT NOT NULL, `merchant_id` INTEGER, `date` TEXT NOT NULL, `payment_status` TEXT NOT NULL, `frequency` TEXT NOT NULL, `amount` TEXT NOT NULL, `unpayable` INTEGER NOT NULL, PRIMARY KEY(`bill_payment_id`))")
                database.execSQL("CREATE INDEX `index_bill_payment_bill_payment_id` ON `bill_payment` (`bill_payment_id`)")
                database.execSQL("CREATE INDEX `index_bill_payment_bill_id` ON `bill_payment` (`bill_id`)")
                database.execSQL("CREATE INDEX `index_bill_payment_merchant_id` ON `bill_payment` (`merchant_id`)")

                database.execSQL("ALTER TABLE `message` ADD COLUMN `auto_dismiss` INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_3_4: Migration = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // New changes in this migration:
                // 1) Create table transaction_user_tags
                // 2) Alter transaction_model table - add column user_tags

                database.execSQL("CREATE TABLE IF NOT EXISTS transaction_user_tags (name TEXT NOT NULL, count INTEGER DEFAULT 0, last_used_at TEXT, created_at TEXT, PRIMARY KEY(name))")
                database.execSQL("CREATE  INDEX index_transaction_user_tags_name ON transaction_user_tags (name)")
                database.execSQL("ALTER TABLE transaction_model ADD COLUMN `user_tags` TEXT")
            }
        }

        private val MIGRATION_4_5: Migration = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // New changes in this migration:
                // 1) Add new table/entity goal and create indexes
                // 2) Add new table/entity goal_period and create indexes
                // 3) Alter table/entity account - add column: "goal_ids"

                database.execSQL("CREATE TABLE IF NOT EXISTS `goal` (`goal_id` INTEGER NOT NULL, `name` TEXT NOT NULL, `description` TEXT, `image_url` TEXT, `account_id` INTEGER, `type` TEXT, `sub_type` TEXT, `tracking_status` TEXT NOT NULL, `tracking_type` TEXT NOT NULL, `status` TEXT NOT NULL, `frequency` TEXT NOT NULL, `target` TEXT NOT NULL, `currency` TEXT NOT NULL, `current_amount` TEXT NOT NULL, `period_amount` TEXT NOT NULL, `start_amount` TEXT NOT NULL, `target_amount` TEXT NOT NULL, `start_date` TEXT NOT NULL, `end_date` TEXT NOT NULL, `estimated_end_date` TEXT, `estimated_target_amount` TEXT, `periods_count` INTEGER NOT NULL, `c_period_goal_period_id` INTEGER NOT NULL, `c_period_goal_id` INTEGER NOT NULL, `c_period_start_date` TEXT NOT NULL, `c_period_end_date` TEXT NOT NULL, `c_period_tracking_status` TEXT NOT NULL, `c_period_current_amount` TEXT NOT NULL, `c_period_target_amount` TEXT NOT NULL, `c_period_required_amount` TEXT NOT NULL, PRIMARY KEY(`goal_id`))")
                database.execSQL("CREATE  INDEX `index_goal_goal_id` ON `goal` (`goal_id`)")
                database.execSQL("CREATE  INDEX `index_goal_account_id` ON `goal` (`account_id`)")
                database.execSQL("CREATE TABLE IF NOT EXISTS `goal_period` (`goal_period_id` INTEGER NOT NULL, `goal_id` INTEGER NOT NULL, `start_date` TEXT NOT NULL, `end_date` TEXT NOT NULL, `tracking_status` TEXT, `current_amount` TEXT NOT NULL, `target_amount` TEXT NOT NULL, `required_amount` TEXT NOT NULL, PRIMARY KEY(`goal_period_id`))")
                database.execSQL("CREATE  INDEX `index_goal_period_goal_period_id` ON `goal_period` (`goal_period_id`)")
                database.execSQL("CREATE  INDEX `index_goal_period_goal_id` ON `goal_period` (`goal_id`)")
                database.execSQL("ALTER TABLE `account` ADD COLUMN `goal_ids` TEXT")
            }
        }

        private val MIGRATION_5_6: Migration = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // New changes in this migration:
                // 1) Alter provider_account table - add column external_id
                // 2) Alter account table - add column external_id
                // 3) Alter transaction_model table - add column external_id
                // 4) Alter table/entity merchant - make smallLogoUrl nullable

                database.execSQL("ALTER TABLE `provider_account` ADD COLUMN `external_id` TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE `account` ADD COLUMN `external_id` TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE `transaction_model` ADD COLUMN `external_id` TEXT NOT NULL DEFAULT ''")

                // START - Alter column smallLogoUrl
                database.execSQL("BEGIN TRANSACTION")
                database.execSQL("DROP INDEX IF EXISTS index_merchant_merchant_id")
                database.execSQL("ALTER TABLE merchant RENAME TO orig_merchant")
                database.execSQL("CREATE TABLE IF NOT EXISTS `merchant` (`merchant_id` INTEGER NOT NULL, `name` TEXT NOT NULL, `merchant_type` TEXT NOT NULL, `small_logo_url` TEXT, PRIMARY KEY(`merchant_id`))")
                database.execSQL("CREATE INDEX `index_merchant_merchant_id` ON `merchant` (`merchant_id`)")
                database.execSQL("INSERT INTO merchant(merchant_id, name, merchant_type, small_logo_url) SELECT merchant_id, name, merchant_type, small_logo_url FROM orig_merchant")
                database.execSQL("DROP TABLE orig_merchant")
                database.execSQL("COMMIT")
                // END - Alter column smallLogoUrl
            }
        }

        private val MIGRATION_6_7: Migration = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // New changes in this migration:
                // 1) Alter report_transaction_history table - add column transaction_tags
                // 2) Alter report_group_transaction_history table - add column transaction_tags
                // 3) Alter goals table - make current period columns nullable

                commonMigrationsReports6To8(database)

                // START - Alter column current period columns to make them nullable
                database.execSQL("BEGIN TRANSACTION")
                database.execSQL("DROP INDEX IF EXISTS `index_goal_goal_id`")
                database.execSQL("DROP INDEX IF EXISTS  `index_goal_account_id`")
                database.execSQL("ALTER TABLE goal RENAME TO original_goal")
                database.execSQL("CREATE TABLE IF NOT EXISTS `goal` (`goal_id` INTEGER NOT NULL, `name` TEXT NOT NULL, `description` TEXT, `image_url` TEXT, `account_id` INTEGER, `type` TEXT, `sub_type` TEXT, `tracking_status` TEXT NOT NULL, `tracking_type` TEXT NOT NULL, `status` TEXT NOT NULL, `frequency` TEXT NOT NULL, `target` TEXT NOT NULL, `currency` TEXT NOT NULL, `current_amount` TEXT NOT NULL, `period_amount` TEXT NOT NULL, `start_amount` TEXT NOT NULL, `target_amount` TEXT NOT NULL, `start_date` TEXT NOT NULL, `end_date` TEXT NOT NULL, `estimated_end_date` TEXT, `estimated_target_amount` TEXT, `periods_count` INTEGER NOT NULL, `c_period_goal_period_id` INTEGER, `c_period_goal_id` INTEGER, `c_period_start_date` TEXT, `c_period_end_date` TEXT, `c_period_tracking_status` TEXT, `c_period_current_amount` TEXT, `c_period_target_amount` TEXT, `c_period_required_amount` TEXT, PRIMARY KEY(`goal_id`))")
                database.execSQL("INSERT INTO goal(goal_id, name, description, image_url, account_id,type,sub_type,tracking_status, tracking_type, status, frequency, target, currency, current_amount, period_amount, start_amount, target_amount, start_date, end_date, estimated_end_date, estimated_target_amount, periods_count, c_period_goal_period_id, c_period_goal_id, c_period_start_date , c_period_end_date, c_period_tracking_status, c_period_current_amount, c_period_target_amount, c_period_required_amount) SELECT goal_id, name, description, image_url,account_id,type,sub_type,tracking_status, tracking_type, status, frequency, target, currency, current_amount, period_amount, start_amount, target_amount, start_date, end_date, estimated_end_date, estimated_target_amount, periods_count, c_period_goal_period_id, c_period_goal_id, c_period_start_date , c_period_end_date, c_period_tracking_status, c_period_current_amount, c_period_target_amount, c_period_required_amount FROM original_goal")
                database.execSQL("DROP TABLE original_goal")
                database.execSQL("CREATE  INDEX `index_goal_goal_id` ON `goal` (`goal_id`)")
                database.execSQL("CREATE  INDEX `index_goal_account_id` ON `goal` (`account_id`)")
                database.execSQL("COMMIT")
                // END - Alter column current period columns to make them nullable
            }
        }

        private fun commonMigrationsReports6To8(database: SupportSQLiteDatabase) {
            database.execSQL("DROP INDEX IF EXISTS `index_report_transaction_history_date_period_filtered_budget_category_report_grouping`")
            database.execSQL("ALTER TABLE `report_transaction_history` ADD COLUMN `transaction_tags` TEXT")
            database.execSQL("CREATE UNIQUE INDEX `index_report_transaction_history_date_period_filtered_budget_category_report_grouping_transaction_tags` ON `report_transaction_history` (`date`, `period`, `filtered_budget_category`, `report_grouping`, `transaction_tags`)")

            database.execSQL("DROP INDEX IF EXISTS `index_report_group_transaction_history_linked_id_date_period_filtered_budget_category_report_grouping`")
            database.execSQL("ALTER TABLE `report_group_transaction_history` ADD COLUMN `transaction_tags` TEXT")
            database.execSQL("CREATE UNIQUE INDEX `index_report_group_transaction_history_linked_id_date_period_filtered_budget_category_report_grouping_transaction_tags` ON `report_group_transaction_history` (`linked_id`, `date`, `period`, `filtered_budget_category`, `report_grouping`, `transaction_tags`)")
        }

        private val MIGRATION_6_8: Migration = object : Migration(6, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // New changes in this migration:
                // 1) Alter report_transaction_history table - add column transaction_tags
                // 2) Alter report_group_transaction_history table - add column transaction_tags
                // 3) Alter goals table - make current period columns nullable, delete columns type and sub_type, add column metadata, add column c_period_period_index
                // 4) Alter goal_period table - add column period_index
                // 5) Alter messages table - delete column action_open_external, add column metadata, add column action_open_mode

                commonMigrationsReports6To8(database)
                commonMigrationsGoals7To8(database)
                commonMigrationsMessages7To8(database)
            }
        }

        private val MIGRATION_7_8: Migration = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // New changes in this migration:
                // 1) Alter goals table - delete columns type and sub_type, add column metadata, add column c_period_period_index
                // 2) Alter goal_period table - add column period_index
                // 3) Alter messages table - delete column action_open_external, add column metadata, add column action_open_mode
                commonMigrationsGoals7To8(database)
                commonMigrationsMessages7To8(database)
            }
        }

        private fun commonMigrationsGoals7To8(database: SupportSQLiteDatabase) {
            // START - Alter columns of goal & goal_period tables
            database.execSQL("BEGIN TRANSACTION")

            database.execSQL("DROP INDEX IF EXISTS `index_goal_goal_id`")
            database.execSQL("DROP INDEX IF EXISTS  `index_goal_account_id`")
            database.execSQL("ALTER TABLE goal RENAME TO original_goal")
            database.execSQL("CREATE TABLE IF NOT EXISTS `goal` (`goal_id` INTEGER NOT NULL, `name` TEXT NOT NULL, `description` TEXT, `image_url` TEXT, `account_id` INTEGER, `tracking_status` TEXT NOT NULL, `tracking_type` TEXT NOT NULL, `status` TEXT NOT NULL, `frequency` TEXT NOT NULL, `target` TEXT NOT NULL, `currency` TEXT NOT NULL, `current_amount` TEXT NOT NULL, `period_amount` TEXT NOT NULL, `start_amount` TEXT NOT NULL, `target_amount` TEXT NOT NULL, `start_date` TEXT NOT NULL, `end_date` TEXT NOT NULL, `estimated_end_date` TEXT, `estimated_target_amount` TEXT, `periods_count` INTEGER NOT NULL, `metadata` TEXT, `c_period_goal_period_id` INTEGER, `c_period_goal_id` INTEGER, `c_period_start_date` TEXT, `c_period_end_date` TEXT, `c_period_tracking_status` TEXT, `c_period_current_amount` TEXT, `c_period_target_amount` TEXT, `c_period_required_amount` TEXT, `c_period_period_index` INTEGER, PRIMARY KEY(`goal_id`))")
            database.execSQL("INSERT INTO goal(goal_id, name, description, image_url, account_id, tracking_status, tracking_type, status, frequency, target, currency, current_amount, period_amount, start_amount, target_amount, start_date, end_date, estimated_end_date, estimated_target_amount, periods_count, c_period_goal_period_id, c_period_goal_id, c_period_start_date , c_period_end_date, c_period_tracking_status, c_period_current_amount, c_period_target_amount, c_period_required_amount) SELECT goal_id, name, description, image_url, account_id, tracking_status, tracking_type, status, frequency, target, currency, current_amount, period_amount, start_amount, target_amount, start_date, end_date, estimated_end_date, estimated_target_amount, periods_count, c_period_goal_period_id, c_period_goal_id, c_period_start_date , c_period_end_date, c_period_tracking_status, c_period_current_amount, c_period_target_amount, c_period_required_amount FROM original_goal")
            database.execSQL("DROP TABLE original_goal")
            database.execSQL("CREATE  INDEX `index_goal_goal_id` ON `goal` (`goal_id`)")
            database.execSQL("CREATE  INDEX `index_goal_account_id` ON `goal` (`account_id`)")

            database.execSQL("DROP INDEX IF EXISTS `index_goal_period_goal_period_id`")
            database.execSQL("DROP INDEX IF EXISTS  `index_goal_period_goal_id`")
            database.execSQL("ALTER TABLE goal_period RENAME TO original_goal_period")
            database.execSQL("CREATE TABLE IF NOT EXISTS `goal_period` (`goal_period_id` INTEGER NOT NULL, `goal_id` INTEGER NOT NULL, `start_date` TEXT NOT NULL, `end_date` TEXT NOT NULL, `tracking_status` TEXT, `current_amount` TEXT NOT NULL, `target_amount` TEXT NOT NULL, `required_amount` TEXT NOT NULL, `period_index` INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(`goal_period_id`))")
            database.execSQL("INSERT INTO goal_period(goal_period_id, goal_id, start_date, end_date, tracking_status, current_amount, target_amount, required_amount) SELECT goal_period_id, goal_id, start_date, end_date, tracking_status, current_amount, target_amount, required_amount FROM original_goal_period")
            database.execSQL("DROP TABLE original_goal_period")
            database.execSQL("CREATE  INDEX `index_goal_period_goal_period_id` ON `goal_period` (`goal_period_id`)")
            database.execSQL("CREATE  INDEX `index_goal_period_goal_id` ON `goal_period` (`goal_id`)")

            database.execSQL("COMMIT")
            // END - Alter columns of goal & goal_period tables
        }

        private fun commonMigrationsMessages7To8(database: SupportSQLiteDatabase) {
            // START - Alter columns of messages table
            database.execSQL("BEGIN TRANSACTION")
            database.execSQL("DROP INDEX IF EXISTS  `index_message_msg_id`")
            database.execSQL("DROP TABLE message")
            database.execSQL("CREATE TABLE IF NOT EXISTS `message` (`msg_id` INTEGER NOT NULL, `event` TEXT NOT NULL, `user_event_id` INTEGER, `placement` INTEGER NOT NULL, `persists` INTEGER NOT NULL, `read` INTEGER NOT NULL, `interacted` INTEGER NOT NULL, `message_types` TEXT NOT NULL, `title` TEXT, `content_type` TEXT NOT NULL, `auto_dismiss` INTEGER NOT NULL, `metadata` TEXT, `content_main` TEXT, `content_header` TEXT, `content_footer` TEXT, `content_text` TEXT, `content_image_url` TEXT, `content_design_type` TEXT, `content_url` TEXT, `content_width` REAL, `content_height` REAL, `content_autoplay` INTEGER, `content_autoplay_cellular` INTEGER, `content_icon_url` TEXT, `content_muted` INTEGER, `action_title` TEXT, `action_link` TEXT, `action_open_mode` TEXT, PRIMARY KEY(`msg_id`))")
            database.execSQL("CREATE  INDEX `index_message_msg_id` ON `message` (`msg_id`)")
            database.execSQL("COMMIT")
            // END - Alter columns of messages table
        }

        private val MIGRATION_8_9: Migration = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // New changes in this migration:
                // 1) Delete table report_transaction_current
                // 2) Delete table report_transaction_history
                // 3) Delete table report_group_transaction_history
                // 4) Create table budget
                // 5) Create table budget_period
                // 6) Alter provider table - add column aggregator_type, permissions

                database.execSQL("DROP INDEX IF EXISTS `index_report_transaction_current_report_id`")
                database.execSQL("DROP INDEX IF EXISTS `index_report_transaction_current_linked_id_day_filtered_budget_category_report_grouping`")
                database.execSQL("DROP TABLE report_transaction_current")
                database.execSQL("DROP INDEX IF EXISTS `index_report_transaction_history_report_id`")
                database.execSQL("DROP INDEX IF EXISTS `index_report_transaction_history_date_period_filtered_budget_category_report_grouping_transaction_tags`")
                database.execSQL("DROP TABLE report_transaction_history")
                database.execSQL("DROP INDEX IF EXISTS `index_report_group_transaction_history_report_group_id`")
                database.execSQL("DROP INDEX IF EXISTS `index_report_group_transaction_history_linked_id_date_period_filtered_budget_category_report_grouping_transaction_tags`")
                database.execSQL("DROP TABLE report_group_transaction_history")

                database.execSQL("CREATE TABLE IF NOT EXISTS `budget` (`budget_id` INTEGER NOT NULL, `is_current` INTEGER NOT NULL, `image_url` TEXT, `tracking_status` TEXT NOT NULL, `status` TEXT NOT NULL, `frequency` TEXT NOT NULL, `user_id` INTEGER NOT NULL, `currency` TEXT NOT NULL, `current_amount` TEXT NOT NULL, `period_amount` TEXT NOT NULL, `start_date` TEXT, `type` TEXT NOT NULL, `type_value` TEXT NOT NULL, `periods_count` INTEGER NOT NULL, `metadata` TEXT, `c_period_budget_period_id` INTEGER, `c_period_budget_id` INTEGER, `c_period_start_date` TEXT, `c_period_end_date` TEXT, `c_period_current_amount` TEXT, `c_period_target_amount` TEXT, `c_period_required_amount` TEXT, `c_period_tracking_status` TEXT, `c_period_index` INTEGER, PRIMARY KEY(`budget_id`))")
                database.execSQL("CREATE  INDEX `index_budget_budget_id` ON `budget` (`budget_id`)")
                database.execSQL("CREATE TABLE IF NOT EXISTS `budget_period` (`budget_period_id` INTEGER NOT NULL, `budget_id` INTEGER NOT NULL, `start_date` TEXT NOT NULL, `end_date` TEXT NOT NULL, `current_amount` TEXT NOT NULL, `target_amount` TEXT NOT NULL, `required_amount` TEXT NOT NULL, `tracking_status` TEXT NOT NULL, `index` INTEGER NOT NULL, PRIMARY KEY(`budget_period_id`))")
                database.execSQL("CREATE  INDEX `index_budget_period_budget_period_id` ON `budget_period` (`budget_period_id`)")
                database.execSQL("CREATE  INDEX `index_budget_period_budget_id` ON `budget_period` (`budget_id`)")

                database.execSQL("ALTER TABLE `provider` ADD COLUMN `aggregator_type` TEXT NOT NULL DEFAULT 'YODLEE'")
                database.execSQL("ALTER TABLE `provider` ADD COLUMN `permissions` TEXT")
            }
        }

        private val MIGRATION_9_10: Migration = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // New changes in this migration:
                // 1) Update tables with new tracking_status values - budget, budget_period, goal, goal_period

                database.execSQL(
                    "UPDATE budget  SET tracking_status = (CASE " +
                        " WHEN tracking_status = 'BEHIND' THEN 'BELOW'" +
                        " WHEN tracking_status = 'ON_TRACK' THEN 'EQUAL'" +
                        " WHEN tracking_status = 'AHEAD' THEN 'ABOVE'" +
                        " ELSE 'EQUAL'" +
                        " END) ," +
                        " c_period_tracking_status = (CASE  " +
                        " WHEN c_period_tracking_status = 'BEHIND' THEN 'BELOW'" +
                        " WHEN c_period_tracking_status = 'ON_TRACK' THEN 'EQUAL'" +
                        " WHEN c_period_tracking_status = 'AHEAD' THEN 'ABOVE'" +
                        " ELSE 'EQUAL'" +
                        " END)"
                )

                database.execSQL(
                    "UPDATE goal  SET tracking_status = (CASE " +
                        " WHEN tracking_status = 'BEHIND' THEN 'BELOW'" +
                        " WHEN tracking_status = 'ON_TRACK' THEN 'EQUAL'" +
                        " WHEN tracking_status = 'AHEAD' THEN 'ABOVE'" +
                        " ELSE 'EQUAL'" +
                        " END) ," +
                        " c_period_tracking_status = (CASE  " +
                        " WHEN c_period_tracking_status = 'BEHIND' THEN 'BELOW'" +
                        " WHEN c_period_tracking_status = 'ON_TRACK' THEN 'EQUAL'" +
                        " WHEN c_period_tracking_status = 'AHEAD' THEN 'ABOVE'" +
                        " ELSE 'EQUAL'" +
                        " END)"
                )

                database.execSQL(
                    "UPDATE budget_period  SET tracking_status = (CASE " +
                        " WHEN tracking_status = 'BEHIND' THEN 'BELOW'" +
                        " WHEN tracking_status = 'ON_TRACK' THEN 'EQUAL'" +
                        " WHEN tracking_status = 'AHEAD' THEN 'ABOVE'" +
                        " ELSE 'EQUAL'" +
                        " END)"
                )

                database.execSQL(
                    "UPDATE goal_period  SET tracking_status = (CASE " +
                        " WHEN tracking_status = 'BEHIND' THEN 'BELOW'" +
                        " WHEN tracking_status = 'ON_TRACK' THEN 'EQUAL'" +
                        " WHEN tracking_status = 'AHEAD' THEN 'ABOVE'" +
                        " ELSE 'EQUAL'" +
                        " END)"
                )
            }
        }
    }
}
