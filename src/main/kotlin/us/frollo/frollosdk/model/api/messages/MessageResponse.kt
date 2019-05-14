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

package us.frollo.frollosdk.model.api.messages

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import us.frollo.frollosdk.model.coredata.messages.Action
import us.frollo.frollosdk.model.coredata.messages.ContentType

/**
 * Declaring the column info allows for the renaming of variables without implementing a
 * database migration, as the column name would not change.
 */
@Entity(tableName = "message",
        indices = [Index("msg_id")])
internal data class MessageResponse(
    @PrimaryKey @ColumnInfo(name = "msg_id") @SerializedName("id") val messageId: Long,
    @ColumnInfo(name = "event") @SerializedName("event") val event: String,
    @ColumnInfo(name = "user_event_id") @SerializedName("user_event_id") val userEventId: Long?,
    @ColumnInfo(name = "placement") @SerializedName("placement") val placement: Long, // 1
    @ColumnInfo(name = "persists") @SerializedName("persists") val persists: Boolean,
    @ColumnInfo(name = "read") @SerializedName("read") val read: Boolean,
    @ColumnInfo(name = "interacted") @SerializedName("interacted") val interacted: Boolean,
    @ColumnInfo(name = "message_types") @SerializedName("message_types") val messageTypes: List<String>,
    @ColumnInfo(name = "title") @SerializedName("title") val title: String?,
    @ColumnInfo(name = "content_type") @SerializedName("content_type") val contentType: ContentType,
    @Embedded(prefix = "content_") @SerializedName("content") val content: MessageContent?,
    @Embedded(prefix = "action_") @SerializedName("action") val action: Action?,
    @ColumnInfo(name = "auto_dismiss") @SerializedName("auto_dismiss") val autoDismiss: Boolean
)