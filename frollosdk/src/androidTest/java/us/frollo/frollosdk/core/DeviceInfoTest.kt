package us.frollo.frollosdk.core

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert
import org.junit.Test
import org.junit.Before

class DeviceInfoTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
    private lateinit var di: DeviceInfo

    @Before
    fun setUp() {
        di = DeviceInfo(context)
    }

    @Test
    fun testDeviceIdNotNullOrEmpty() {
        Assert.assertNotEquals("", di.deviceId)
        Assert.assertNotNull(di.deviceId)
    }

    @Test
    fun testDeviceNameNotNullOrEmpty() {
        Assert.assertNotEquals("", di.deviceName)
        Assert.assertNotNull(di.deviceName)
    }

    @Test
    fun testDeviceTypeNotNullOrEmpty() {
        Assert.assertNotEquals("", di.deviceType)
        Assert.assertNotNull(di.deviceType)
    }
}