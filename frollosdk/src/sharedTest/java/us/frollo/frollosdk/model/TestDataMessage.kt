package us.frollo.frollosdk.model

import us.frollo.frollosdk.model.api.messages.MessageContent
import us.frollo.frollosdk.model.api.messages.MessageResponse
import us.frollo.frollosdk.model.coredata.messages.Action
import us.frollo.frollosdk.model.coredata.messages.ContentType
import us.frollo.frollosdk.testutils.*
import kotlin.random.Random

internal fun testMessageResponseData(type: ContentType? = null, types: List<String>? = null, read: Boolean? = null, msgId: Long? = null) : MessageResponse {
    val htmlContent = MessageContent(
            footer = randomString(20),
            header = randomString(20),
            main = "<html></html>")
    val imageContent = MessageContent(
            height = randomNumber(1..1000).toDouble(),
            url = "https://example.com/image.png",
            width = randomNumber(1..1000).toDouble())
    val textContent = MessageContent(
            designType = "information",
            footer = randomString(20),
            header = randomString(20),
            imageUrl = "https://example.com/image.png",
            text = randomString(200))
    val videoContent = MessageContent(
            autoplay = randomBoolean(),
            autoplayCellular = randomBoolean(),
            height = randomNumber(1..1000).toDouble(),
            iconUrl = "https://example.com/image.png",
            muted = randomBoolean(),
            url = "https://example.com/video.mp4",
            width = randomNumber(1..1000).toDouble())

    val contentType = type?.let { it } ?: ContentType.values()[Random.nextInt(ContentType.values().size)]
    val content = when(contentType) {
        ContentType.TEXT -> textContent
        ContentType.IMAGE -> imageContent
        ContentType.VIDEO -> videoContent
        ContentType.HTML -> htmlContent
    }

    return MessageResponse(
            messageId = if (msgId == null) randomNumber().toLong() else msgId,
            action = Action(link = "frollo://dashboard", openExternal = randomBoolean(), title = randomString(30)),
            contentType = contentType,
            content = content,
            event = randomString(30),
            interacted = randomBoolean(),
            messageTypes = if (types == null) mutableListOf("home_nudge") else types,
            persists = randomBoolean(),
            placement = randomNumber(1..1000).toLong(),
            read = if (read == null) randomBoolean() else read,
            title = randomString(100),
            userEventId = randomNumber(1..100000).toLong())
}

internal fun MessageResponse.testModifyUserResponseData(newTitle: String? = null, types: List<String>? = null) : MessageResponse {
    return MessageResponse(
            messageId = messageId,
            action = action,
            contentType = contentType,
            content = content,
            event = event,
            interacted = interacted,
            messageTypes = if (types == null) messageTypes else types,
            persists = persists,
            placement = placement,
            read = read,
            title = if (newTitle == null) title else newTitle,
            userEventId = userEventId)
}