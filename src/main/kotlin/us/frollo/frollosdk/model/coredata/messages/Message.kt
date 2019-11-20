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
import us.frollo.frollosdk.model.IAdapterModel

/** Data representation of a Message */
abstract class Message(

    /** Unique identifier of the message */
    open val messageId: Long,

    /** Event name associated with the message */
    open val event: String,

    /** Unique ID of the user event associated with the message */
    open val userEventId: Long?,

    /** Placement order of the message - higher is more important */
    open val placement: Long,

    /** Indicates if the message can be auto dismissed after user has interacted */
    open val autoDismiss: Boolean,

    /** Indicates if the message can be marked read or not */
    open val persists: Boolean,

    /** Read/unread state */
    open val read: Boolean,

    /** Indicates if the user has interacted with the message */
    open val interacted: Boolean,

    /** All message types the message should be displayed in */
    open val messageTypes: List<String>,

    /** Title of the message */
    open val title: String?,

    /** Type of content the message contains, indicates subclasses of [Message] */
    open val contentType: ContentType,

    /** Action data containing the URL the user should be taken to when interacting with a message. Can be a deeplink or web URL. */
    open val action: Action?,

    /** Metadata - custom JSON associated with the message (Optional) */
    open val metadata: JsonObject?

) : IAdapterModel