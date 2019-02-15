package us.frollo.frollosdksample

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import org.jetbrains.anko.AlertBuilder
import org.jetbrains.anko.alert
import us.frollo.frollosdk.model.coredata.aggregation.accounts.Balance
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

fun <T1, T2> ifNotNull(value1: T1?, value2: T2?, bothNotNull: (T1, T2) -> (Unit)) {
    if (value1 != null && value2 != null) {
        bothNotNull(value1, value2)
    }
}

fun <T> LiveData<T>.observe(owner: LifecycleOwner, observer: (T?) -> Unit) =
        observe(owner, Observer<T> { v -> observer.invoke(v) })

fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToParent: Boolean = false): View =
        LayoutInflater.from(context).inflate(layoutRes, this, attachToParent)

fun View.show() {
    this.visibility = View.VISIBLE
}

fun View.hide(visibility: Int = View.GONE) {
    this.visibility = visibility
}

fun AlertBuilder<AlertDialog>.showThemed(): AlertDialog =
        show().setTheme()

fun AlertBuilder<DialogInterface>.showThemed(): DialogInterface =
        show().apply { (this as? AlertDialog)?.setTheme() }

fun DialogInterface.showThemed(): DialogInterface =
        apply { (this as? AlertDialog)?.setTheme() }

private fun AlertDialog.setTheme(): AlertDialog =
        apply {
            getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(ResourcesCompat.getColor(context.resources, R.color.colorPrimary, context.theme))
            getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(ResourcesCompat.getColor(context.resources, R.color.colorPrimary, context.theme))
            getButton(AlertDialog.BUTTON_NEUTRAL)?.setTextColor(ResourcesCompat.getColor(context.resources, R.color.colorPrimary, context.theme))
        }

fun Fragment.displayError(message: String?, title: String)
        = requireActivity().displayError(message, title)

fun Activity.displayError(message: String?, title: String) {
    alert(message ?: "", title) {
        positiveButton("OK", {})
    }.showThemed()
}

fun Date.toString(format: String): String =
        SimpleDateFormat(format, Locale.getDefault()).format(this)

fun Number.toCurrencyString(currency: String, fractionDigits: Int = 2): String {
    val format = NumberFormat.getCurrencyInstance(Locale.getDefault())
    //Try setting the currency into the new format if possible. If not, go with Locale default
    try {
        format.currency = Currency.getInstance(currency)
    } catch (ex: Exception) {
        Log.e("Number.toCurrencyString", "Unable to format `$this` to currency")
    }
    format.maximumFractionDigits = fractionDigits
    return format.format(this)
}

val Balance?.display: String?
    get() {
        this?.let {
            return it.amount.toCurrencyString(it.currency, 2)
        }
        return null
    }