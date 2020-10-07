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

package us.frollo.frollosdk.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery
import io.reactivex.Observable
import us.frollo.frollosdk.model.coredata.images.Image

@Dao
internal interface ImageDao {

    @Query("SELECT * FROM image")
    fun load(): LiveData<List<Image>>

    @Query("SELECT * FROM image WHERE image_id = :imageId")
    fun load(imageId: Long): LiveData<Image?>

    @RawQuery(observedEntities = [Image::class])
    fun loadByQuery(queryStr: SupportSQLiteQuery): LiveData<List<Image>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg models: Image): LongArray

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(model: Image): Long

    @RawQuery
    fun getIdsByQuery(queryStr: SupportSQLiteQuery): List<Long>

    @Query("DELETE FROM image WHERE image_id IN (:imageIds)")
    fun deleteMany(imageIds: LongArray)

    @Query("DELETE FROM image WHERE image_id = :imageId")
    fun delete(imageId: Long)

    @Query("DELETE FROM image")
    fun clear()

    /**
     * RxJava Return Types
     */

    @Query("SELECT * FROM image")
    fun loadRx(): Observable<List<Image>>

    @Query("SELECT * FROM image WHERE image_id = :imageId")
    fun loadRx(imageId: Long): Observable<Image?>

    @RawQuery(observedEntities = [Image::class])
    fun loadByQueryRx(queryStr: SupportSQLiteQuery): Observable<List<Image>>
}
