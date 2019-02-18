package us.frollo.frollosdk.model.coredata.messages

/** Data representation of a TEXT Message */
data class MessageText(
        /** Unique identifier of the message */
        override val messageId: Long,
        /** Event name associated with the message */
        override val event: String,
        /** Unique ID of the user event associated with the message */
        override val userEventId: Long?,
        /** Placement order of the message - higher is more important */
        override val placement: Long,
        /** Indicates if the message can be marked read or not */
        override val persists: Boolean,
        /** Read/unread state */
        override val read: Boolean,
        /** Indicates if the user has interacted with the message */
        override val interacted: Boolean,
        /** All message types the message should be displayed in */
        override val messageTypes: List<String>,
        /** Title of the message */
        override val title: String?,
        /** Type of content the message contains, indicates subclasses of [Message] */
        override val contentType: ContentType,
        /** Action data containing the URL the user should be taken to when interacting with a message. Can be a deeplink or web URL. */
        override val action: Action?,
        /** Design type for the message */
        val designType: String,
        /** Footer content */
        val footer: String?,
        /** Header content */
        val header: String?,
        /** Raw value for the image URL */
        val imageUrl: String?,
        /** Text body content */
        val text: String?
) : Message(messageId, event, userEventId, placement, persists, read, interacted, messageTypes, title, contentType, action)