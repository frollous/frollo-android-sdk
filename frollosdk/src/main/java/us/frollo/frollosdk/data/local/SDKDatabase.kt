package us.frollo.frollosdk.data.local

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import us.frollo.frollosdk.data.local.dao.UserDao
import us.frollo.frollosdk.model.api.user.UserResponse

// TODO: PLAN FOR CONTINGENCY (DB CORRUPTED)
@Database(entities = [
    UserResponse::class
], version = 1, exportSchema = true)

@TypeConverters(Converters::class)
abstract class SDKDatabase : RoomDatabase() {
    internal abstract fun users(): UserDao

    companion object {
        private const val DATABASE_NAME = "frollosdk-db"
        // For Singleton instantiation
        @Volatile private var instance: SDKDatabase? = null

        internal fun getInstance(app: Application): SDKDatabase {
            return instance ?: synchronized(this) {
                instance ?: create(app).also { instance = it }
            }
        }

        private fun create(app: Application): SDKDatabase =
                Room.databaseBuilder(app, SDKDatabase::class.java, DATABASE_NAME)
                        //.addMigrations(MIGRATION_1_2)
                        .build()
        /**
         * Copy-paste of auto-generated SQLs from room schema json file
         * located in sandbox code after building under app/schemas/$version.json
         */
        /*private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {

            }
        }*/
    }

    internal fun clearAll() {
        users().clear()
    }
}