package us.frollo.frollosdksample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import us.frollo.frollosdksample.base.ViewLifecycleFragment

class AccountsFragment : ViewLifecycleFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_accounts, container, false)
    }
}
