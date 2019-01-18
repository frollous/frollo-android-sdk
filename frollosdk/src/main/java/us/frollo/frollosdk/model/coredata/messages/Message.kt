package us.frollo.frollosdk.model.coredata.messages

abstract class Message(
        open val messageId: Long,
        open val event: String,
        open val userEventId: Long?,
        open val placement: Long,
        open val persists: Boolean,
        open val read: Boolean,
        open val interacted: Boolean,
        open val messageTypes: List<String>,
        open val title: String?,
        open val contentType: ContentType,
        open val action: Action?)