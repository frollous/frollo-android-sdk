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

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import us.frollo.frollosdk.model.api.messages.MessageContent
import us.frollo.frollosdk.model.coredata.messages.ContentType
import us.frollo.frollosdk.model.coredata.messages.MessageHTML
import us.frollo.frollosdk.model.coredata.messages.MessageImage
import us.frollo.frollosdk.model.coredata.messages.MessageText
import us.frollo.frollosdk.model.coredata.messages.MessageVideo
import us.frollo.frollosdk.model.testMessageResponseData
import us.frollo.frollosdk.model.testModifyUserResponseData

class MessageMappingTest {

    @Test
    fun testMessageResponseToHTMLMessage() {
        val messageResponse = testMessageResponseData(type = ContentType.HTML)
        val message = messageResponse.toMessage()
        assertTrue(message is MessageHTML)
        assertNotNull((message as MessageHTML).main)
    }

    @Test
    fun testMessageResponseToTextMessage() {
        val messageResponse = testMessageResponseData(type = ContentType.TEXT)
        val message = messageResponse.toMessage()
        assertTrue(message is MessageText)
        assertNotNull((message as MessageText).designType)
    }

    @Test
    fun testMessageResponseToVideoMessage() {
        val messageResponse = testMessageResponseData(type = ContentType.VIDEO)
        val message = messageResponse.toMessage()
        assertTrue(message is MessageVideo)
        assertNotNull((message as MessageVideo).url)
    }

    @Test
    fun testMessageResponseToImageMessage() {
        val messageResponse = testMessageResponseData(type = ContentType.IMAGE)
        val message = messageResponse.toMessage()
        assertTrue(message is MessageImage)
        assertNotNull((message as MessageImage).url)
    }

    @Test
    fun testMessageResponseToMessageWithNullContent() {
        val messageResponse = testMessageResponseData(type = ContentType.VIDEO)
        val modifiedResponse = messageResponse.testModifyUserResponseData(messageContent = MessageContent())
        val message = modifiedResponse.toMessage()
        assertNull(message)
    }
}
