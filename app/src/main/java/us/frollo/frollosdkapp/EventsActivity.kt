package us.frollo.frollosdkapp

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_events.*
import us.frollo.frollosdk.FrolloSDK
import us.frollo.frollosdk.error.APIError
import us.frollo.frollosdk.error.DataError
import us.frollo.frollosdk.error.FrolloSDKError

class EventsActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "EventsActivity"
    }

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
            else Log.d(TAG,"*** Event triggered")
        }
    }

    private fun handleError(error: FrolloSDKError) {
        when (error) {
            is APIError -> Log.e(TAG,"*** Error: ${error.errorCode} - ${error.localizedDescription}")
            is DataError -> Log.e(TAG,"*** Error: ${error.type} - ${error.localizedDescription}")
            else -> Log.e(TAG,"*** Error: ${error.localizedDescription}")
        }
    }
}
