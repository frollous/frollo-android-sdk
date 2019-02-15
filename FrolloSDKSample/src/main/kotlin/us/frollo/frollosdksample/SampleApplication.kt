package us.frollo.frollosdksample

import android.app.Application
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.jakewharton.threetenabp.AndroidThreeTen
import us.frollo.frollosdk.FrolloSDK

class SampleApplication : Application(), LifecycleObserver {

    override fun onCreate() {
        super.onCreate()

        AndroidThreeTen.init(this)

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {

        FrolloSDK.onAppBackgrounded()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {

        if (FrolloSDK.authentication.loggedIn)
            FrolloSDK.onAppForegrounded()
    }
}
