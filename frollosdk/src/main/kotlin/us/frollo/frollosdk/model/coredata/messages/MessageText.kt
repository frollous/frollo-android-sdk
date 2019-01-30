package us.frollo.frollosdk.model.coredata.messages

data class MessageText(
        override val messageId: Long,
        override val event: String,
        override val userEventId: Long?,
        override val placement: Long,
        override val persists: Boolean,
        override val read: Boolean,
        override val interacted: Boolean,
        override val messageTypes: List<String>,
        override val title: String?,
        override val contentType: ContentType,
        override val action: Action?,
        val designType: String,
        val footer: String?,
        val header: String?,
        val imageUrl: String?,
        val text: String?
) : Message(messageId, event, userEventId, placement, persists, read, interacted, messageTypes, title, contentType, action)