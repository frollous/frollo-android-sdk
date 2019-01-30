package us.frollo.frollosdksample

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import org.jetbrains.anko.AlertBuilder
import org.jetbrains.anko.alert

fun <T> LiveData<T>.observe(owner: LifecycleOwner, observer: (T?) -> Unit) =
        observe(owner, Observer<T> { v -> observer.invoke(v) })

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