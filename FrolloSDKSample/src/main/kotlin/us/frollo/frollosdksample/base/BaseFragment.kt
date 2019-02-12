package us.frollo.frollosdksample.base

import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity

abstract class BaseFragment : ViewLifecycleFragment() {

    protected val activity: AppCompatActivity?
        get() = super.getActivity() as? AppCompatActivity

    protected val actionBar: ActionBar?
        get() = activity?.supportActionBar
}