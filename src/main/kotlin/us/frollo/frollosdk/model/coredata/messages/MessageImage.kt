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

import com.google.gson.JsonObject

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

    /** Indicates if the message can be auto dismissed after user has interacted */
    override val autoDismiss: Boolean,

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
    val url: String,

    override val metadata: JsonObject?

) : Message(messageId, event, userEventId, placement, autoDismiss, persists, read, interacted, messageTypes, title, contentType, action, metadata)