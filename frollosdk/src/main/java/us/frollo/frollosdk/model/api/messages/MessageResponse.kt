package us.frollo.frollosdk.model.api.messages

import androidx.room.*
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
        @PrimaryKey
        @ColumnInfo(name = "msg_id") @SerializedName("id") val messageId: Long,
        @ColumnInfo(name = "event") @SerializedName("event") val event: String,
        @ColumnInfo(name = "user_event_id") @SerializedName("user_event_id") val userEventId: Long?,
        @ColumnInfo(name = "placement") @SerializedName("placement") val placement: Long, //1
        @ColumnInfo(name = "persists") @SerializedName("persists") val persists: Boolean,
        @ColumnInfo(name = "read") @SerializedName("read") val read: Boolean,
        @ColumnInfo(name = "interacted") @SerializedName("interacted") val interacted: Boolean,
        @ColumnInfo(name = "message_types") @SerializedName("message_types") val messageTypes: List<String>,
        @ColumnInfo(name = "title") @SerializedName("title") val title: String?,
        @ColumnInfo(name = "content_type") @SerializedName("content_type") val contentType: ContentType,
        @Embedded(prefix = "content_") @SerializedName("content") val content: MessageContent?,
        @Embedded(prefix = "action_") @SerializedName("action") val action: Action?
)