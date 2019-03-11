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

package us.frollo.frollosdk.model.coredata.messages

import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName

/** Action data in a message */
data class Action(
        /** Title of the action (optional) */
        @ColumnInfo(name = "title") @SerializedName("title") var title: String? = null,
        /** Raw value of the action URL (optional) */
        @ColumnInfo(name = "link") @SerializedName("link") var link: String? = null,
        /** Action should open the link externally or internally. Externally means the system should handle opening the link. */
        @ColumnInfo(name = "open_external") @SerializedName("open_external") var openExternal: Boolean)