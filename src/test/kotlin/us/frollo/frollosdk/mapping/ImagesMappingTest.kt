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

import org.junit.Assert.assertEquals
import org.junit.Test
import us.frollo.frollosdk.model.testImageResponseData

class ImagesMappingTest {

    @Test
    fun testImageResponseToImage() {
        val response = testImageResponseData(imageId = 12345)
        val model = response.toImage()
        assertEquals(12345L, model.imageId)
        assertEquals(2, model.imageTypes.size)
        assertEquals("https://example.com/small/image.png", model.smallImageUrl)
        assertEquals("https://example.com/large/image.png", model.largeImageUrl)
    }
}
