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

package us.frollo.frollosdk.model.coredata.images

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// Declaring the ColumnInfo allows for the renaming of variables without
// implementing a database migration, as the column name would not change.

@Entity(
    tableName = "image",
    indices = [
        Index("image_id")
    ]
)

/** Data representation of a Image */
data class Image(

    /** Unique ID for image */
    @PrimaryKey
    @ColumnInfo(name = "image_id") val imageId: Long,

    /** Name of the image */
    @ColumnInfo(name = "name") val name: String,

    /** All image types the image should be displayed in */
    @ColumnInfo(name = "image_types") val imageTypes: List<String>,

    /** URL to the small image */
    @ColumnInfo(name = "small_image_url") val smallImageUrl: String,

    /** URL to the large image */
    @ColumnInfo(name = "large_image_url") val largeImageUrl: String
)
