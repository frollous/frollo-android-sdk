package us.frollo.frollosdk.model.coredata.messages

/** Data representation of a IMAGE Message */
data class MessageImage(

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

        /** Height of the image in pixels */
        val height: Double?,

        /** Width of the image in pixels */
        val width: Double?,

        /** Raw value for the image URL */
        val url: String

) : Message(messageId, event, userEventId, placement, persists, read, interacted, messageTypes, title, contentType, action)