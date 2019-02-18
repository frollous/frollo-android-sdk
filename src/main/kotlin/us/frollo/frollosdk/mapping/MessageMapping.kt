package us.frollo.frollosdk.mapping

import us.frollo.frollosdk.logging.Log
import us.frollo.frollosdk.model.api.messages.MessageResponse
import us.frollo.frollosdk.model.coredata.messages.*

internal fun MessageResponse.toMessage(): Message? {
    val TAG = "MessageMapping"

    val contentType = this.contentType

    return when (contentType) {
        ContentType.HTML -> {
                content?.main?.let { mainHtml ->
                        MessageHTML(
                                messageId = messageId,
                                event = event,
                                userEventId = userEventId,
                                placement = placement,
                                persists = persists,
                                read = read,
                                interacted = interacted,
                                messageTypes = messageTypes,
                                title = title,
                                contentType = contentType,
                                action = action,
                                footer = content.footer,
                                header = content.header,
                                main = mainHtml)
                } ?: run {
                    Log.e("$TAG#toMessage-HTML", "HTML Message : Invalid data in content")
                        null
                }
        }

        ContentType.TEXT -> {
                content?.designType?.let { designType ->
                        MessageText(
                                messageId = messageId,
                                event = event,
                                userEventId = userEventId,
                                placement = placement,
                                persists = persists,
                                read = read,
                                interacted = interacted,
                                messageTypes = messageTypes,
                                title = title,
                                contentType = contentType,
                                action = action,
                                designType = designType,
                                footer = content.footer,
                                header = content.header,
                                imageUrl = content.imageUrl,
                                text = content.text)
                } ?: run {
                    Log.e("$TAG#toMessage-TEXT", "TEXT Message : Invalid data in content")
                        null
                }
        }

        ContentType.VIDEO -> {
                if (content?.url != null) {
                    MessageVideo(
                            messageId = messageId,
                            event = event,
                            userEventId = userEventId,
                            placement = placement,
                            persists = persists,
                            read = read,
                            interacted = interacted,
                            messageTypes = messageTypes,
                            title = title,
                            contentType = contentType,
                            action = action,
                            height = content.height,
                            width = content.width,
                            muted = content.muted ?: false,
                            autoplay = content.autoplay ?: false,
                            autoplayCellular = content.autoplayCellular ?: false,
                            iconUrl = content.iconUrl,
                            url = content.url)
                } else {
                    Log.e("$TAG#toMessage-VIDEO", "VIDEO Message : Invalid data in content")
                    null
                }
        }

        ContentType.IMAGE -> {
            if (content?.url != null) {
                MessageImage(
                        messageId = messageId,
                        event = event,
                        userEventId = userEventId,
                        placement = placement,
                        persists = persists,
                        read = read,
                        interacted = interacted,
                        messageTypes = messageTypes,
                        title = title,
                        contentType = contentType,
                        action = action,
                        height = content.height,
                        width = content.width,
                        url = content.url)
            } else {
                Log.e("$TAG#toMessage-IMAGE","IMAGE Message : Invalid data in content")
                null
            }
        }
    }
}