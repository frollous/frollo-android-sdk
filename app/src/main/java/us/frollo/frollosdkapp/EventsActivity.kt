package us.frollo.frollosdkapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_events.*
import timber.log.Timber
import us.frollo.frollosdk.FrolloSDK
import us.frollo.frollosdk.error.APIError
import us.frollo.frollosdk.error.DataError
import us.frollo.frollosdk.error.FrolloSDKError

class EventsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_events)

        initView()
    }

    private fun initView() {
        btn_event_trigger.setOnClickListener { triggerEvent() }
    }

    private fun triggerEvent() {
        FrolloSDK.events.triggerEvent(eventName = "APP_BUDGET_RESET", delayMinutes = 1L) { error ->
            if (error != null) handleError(error)
            else Timber.d("*** Event triggered")
        }
    }

    private fun handleError(error: FrolloSDKError) {
        when (error) {
            is APIError -> Timber.d("*** Error: ${error.errorCode} - ${error.localizedDescription}")
            is DataError -> Timber.d("*** Error: ${error.type} - ${error.localizedDescription}")
            else -> Timber.d("*** Error: ${error.localizedDescription}")
        }
    }
}
