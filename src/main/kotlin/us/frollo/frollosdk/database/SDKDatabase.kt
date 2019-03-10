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
import us.frollo.frollosdk.database.dao.*
import us.frollo.frollosdk.model.api.messages.MessageResponse
import us.frollo.frollosdk.model.coredata.aggregation.accounts.Account
import us.frollo.frollosdk.model.coredata.aggregation.merchants.Merchant
import us.frollo.frollosdk.model.coredata.aggregation.provideraccounts.ProviderAccount
import us.frollo.frollosdk.model.coredata.aggregation.providers.Provider
import us.frollo.frollosdk.model.coredata.aggregation.transactioncategories.TransactionCategory
import us.frollo.frollosdk.model.coredata.aggregation.transactions.Transaction
import us.frollo.frollosdk.model.coredata.user.User

@Database(entities = [
    User::class,
    MessageResponse::class,
    Provider::class,
    ProviderAccount::class,
    Account::class,
    Transaction::class,
    TransactionCategory::class,
    Merchant::class
], version = 1, exportSchema = true)

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
                        .fallbackToDestructiveMigration()
                        //.addMigrations(MIGRATION_1_2)
                        .build()
        /**
         * Copy-paste of auto-generated SQLs from room schema json file
         * located in sandbox code after building under app/schemas/$version.json
         */
        /*private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                //to be implemented
            }
        }*/
    }
}