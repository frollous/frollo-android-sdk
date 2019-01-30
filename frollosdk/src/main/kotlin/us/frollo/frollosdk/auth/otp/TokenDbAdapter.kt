/*
 * Copyright Mark McAvoy - www.bitethebullet.co.uk 2009
 *
 * This file is part of Android Token.
 *
 * Android Token is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Android Token is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Android Token.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package us.frollo.frollosdk.auth.otp

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

/**
 * Performs the CRUD database actions for the Android Token application
 */
internal class TokenDbAdapter(val mContext: Context) {
    companion object {
        //const holding the table field names
        const val KEY_TOKEN_ROWID = "_id"
        const val KEY_TOKEN_NAME = "name"
        const val KEY_TOKEN_SERIAL = "serial"
        const val KEY_TOKEN_SEED = "seed"
        const val KEY_TOKEN_COUNT = "eventcount"
        const val KEY_TOKEN_TYPE = "tokentype"
        const val KEY_TOKEN_OTP_LENGTH = "otplength"
        const val KEY_TOKEN_TIME_STEP = "timestep"
        const val KEY_TOKEN_NAME_SORT = "namesort"

        const val KEY_PIN_ROWID = "_id"
        const val KEY_PIN_HASH = "pinhash"

        //const define the different token type
        const val TOKEN_TYPE_EVENT = 0
        const val TOKEN_TYPE_TIME = 1

        const val TAG = "TokenDbAdapter"

        //const database tables, version
        private const val DATABASE_NAME = "androidtoken.db"
        private const val DATABASE_TOKEN_TABLE = "token"
        private const val DATABASE_PIN_TABLE = "pin"
        private const val DATABASE_VERSION = 1

        private const val DATABASE_CREATE_TOKEN = (
                "create table token (_id integer primary key autoincrement,"
                        + " name text not null,"
                        + " serial text,"
                        + " seed text,"
                        + " eventcount integer,"
                        + " tokentype integer,"
                        + " otplength integer,"
                        + " timestep integer,"
                        + " namesort text);")

        private const val DATABASE_CREATE_PIN = "create table pin(_id integer primary key autoincrement," + " pinhash text);"

        private const val DATABASE_DROP_TOKEN = "DROP TABLE IF EXISTS token;"
        private const val DATABASE_DROP_PIN = "DROP TABLE IF EXISTS pin;"
    }

    private var mDbHelper: DatabaseHelper? = null
    private var mDb: SQLiteDatabase? = null

    private inner class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(DATABASE_CREATE_TOKEN)
            db.execSQL(DATABASE_CREATE_PIN)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data")
            db.execSQL(DATABASE_DROP_TOKEN)
            db.execSQL(DATABASE_DROP_PIN)
            onCreate(db)
        }
    }

    @Throws(SQLException::class)
    fun open(): TokenDbAdapter {
        mDbHelper = DatabaseHelper(mContext)
        mDb = mDbHelper?.getWritableDatabase()
        return this
    }

    fun close() {
        mDbHelper?.close()
    }

    // Data Access Methods
    ////////////////////////////////


    //TOKEN TABLE
    fun createToken(name: String, serial: String, seed: String, tokenType: Int, otpLength: Int, timeStep: Int): Long {
        val values = ContentValues()
        values.put(KEY_TOKEN_NAME, name)
        values.put(KEY_TOKEN_SERIAL, serial)
        values.put(KEY_TOKEN_SEED, seed)
        values.put(KEY_TOKEN_TYPE, tokenType)
        values.put(KEY_TOKEN_OTP_LENGTH, otpLength)
        values.put(KEY_TOKEN_TIME_STEP, timeStep)
        values.put(KEY_TOKEN_COUNT, 0)
        values.put(KEY_TOKEN_NAME_SORT, name.toLowerCase())

        return mDb?.insert(DATABASE_TOKEN_TABLE, null, values) ?: -1
    }

    fun deleteToken(tokenId: Long): Boolean {
        return mDb?.let { it.delete(DATABASE_TOKEN_TABLE, "$KEY_TOKEN_ROWID=$tokenId", null) > 0 } ?: run { false }
    }

    fun renameToken(tokenId: Long, name: String): Boolean {
        val values = ContentValues()
        values.put(KEY_TOKEN_NAME, name)

        return mDb?.let { it.update(DATABASE_TOKEN_TABLE, values, "$KEY_TOKEN_ROWID=$tokenId", null) > 0 } ?: run { false }
    }

    fun incrementTokenCount(tokenId: Long) {
        mDb?.execSQL("UPDATE token SET eventcount = eventcount + 1 WHERE _id = $tokenId")
    }

    fun setTokenCounter(tokenId: Long, eventCounter: Int) {
        mDb?.execSQL("UPDATE token SET eventcount = $eventCounter  WHERE _id = $tokenId")
    }

    fun fetchToken(tokenId: Long): Cursor? {
        val c = mDb?.query(DATABASE_TOKEN_TABLE,
                arrayOf(KEY_TOKEN_ROWID, KEY_TOKEN_NAME, KEY_TOKEN_SERIAL, KEY_TOKEN_SEED, KEY_TOKEN_COUNT, KEY_TOKEN_TYPE, KEY_TOKEN_OTP_LENGTH, KEY_TOKEN_TIME_STEP),
                "$KEY_TOKEN_ROWID=$tokenId", null, null, null, null)

        c?.moveToFirst()

        return c
    }

    fun fetchAllTokens(): Cursor? {
        return mDb?.query(DATABASE_TOKEN_TABLE,
                arrayOf(KEY_TOKEN_ROWID, KEY_TOKEN_NAME, KEY_TOKEN_SERIAL, KEY_TOKEN_SEED, KEY_TOKEN_COUNT, KEY_TOKEN_TYPE, KEY_TOKEN_OTP_LENGTH, KEY_TOKEN_TIME_STEP),
                null, null, null, null,
                "$KEY_TOKEN_NAME_SORT ASC")
    }


    //PIN TABLE
    fun createOrUpdatePin(pinHash: String): Boolean {

        var result = false

        val values = ContentValues()
        values.put(KEY_PIN_HASH, pinHash)

        val c = fetchPin()

        if (c!!.count == 0) {
            //no pin set, insert new row
            result = mDb?.let { it.insert(DATABASE_PIN_TABLE, null, values) > 0 } ?: run { false }
        } else {
            //pin already set update existing
            result = mDb?.let { it.update(DATABASE_PIN_TABLE, values, null, null) > 0 } ?: run { false }
        }

        c.close()

        return result
    }

    fun deletePin() {
        mDb?.delete(DATABASE_PIN_TABLE, null, null)
    }

    fun fetchPin(): Cursor? {
        val c = mDb?.query(DATABASE_PIN_TABLE,
                arrayOf(KEY_PIN_HASH), null, null, null, null, null,
                "1")

        c?.moveToFirst()

        return c
    }
}