package us.frollo.frollosdksample.base

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import us.frollo.frollosdksample.ifNotNull

abstract class BaseViewHolder<in T>(itemView: View): RecyclerView.ViewHolder(itemView) {
    private var mClickCallback: (((View), (Int)) -> Unit)? = null
    private var mLongClickCallback: (((View), (Int)) -> Unit)? = null

    protected open fun bindClickHandlers() {
        itemView.setOnClickListener {
            invokeCallback(it)
        }
    }

    protected open fun bindLongClickHandlers() {
        itemView.setOnLongClickListener {
            invokeLongClickCallback(it)
        true
        }
    }

    protected fun invokeCallback(view: View) =
            ifNotNull(mClickCallback, view) { it, _ -> it.invoke(view, adapterPosition) }

    protected open fun invokeLongClickCallback(view: View) =
            ifNotNull(mLongClickCallback, view) { it, _ -> it.invoke(view, adapterPosition) }

    fun setOnClickListener(callback: (((View), (Int)) -> Unit)) {
        bindClickHandlers()
        this.mClickCallback = callback
    }

    fun setOnLongClickListener(callback: (((View), (Int)) -> Unit)) {
        bindLongClickHandlers()
        this.mLongClickCallback = callback
    }

    abstract fun bind(model: T)
    abstract fun recycle()
}