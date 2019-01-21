package us.frollo.frollosdk.mapping

import org.junit.Assert.*
import org.junit.Test
import us.frollo.frollosdk.model.api.messages.MessageContent
import us.frollo.frollosdk.model.coredata.messages.*
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