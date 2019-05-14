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

package us.frollo.frollosdk.mapping

import us.frollo.frollosdk.logging.Log
import us.frollo.frollosdk.model.api.messages.MessageResponse
import us.frollo.frollosdk.model.coredata.messages.ContentType
import us.frollo.frollosdk.model.coredata.messages.Message
import us.frollo.frollosdk.model.coredata.messages.MessageHTML
import us.frollo.frollosdk.model.coredata.messages.MessageImage
import us.frollo.frollosdk.model.coredata.messages.MessageText
import us.frollo.frollosdk.model.coredata.messages.MessageVideo

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
                                autoDismiss = autoDismiss,
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
                                autoDismiss = autoDismiss,
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
                            autoDismiss = autoDismiss,
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
                        autoDismiss = autoDismiss,
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
                Log.e("$TAG#toMessage-IMAGE", "IMAGE Message : Invalid data in content")
                null
            }
        }
    }
}