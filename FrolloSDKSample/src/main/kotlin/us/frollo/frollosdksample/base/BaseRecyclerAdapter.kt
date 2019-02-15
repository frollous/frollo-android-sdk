package us.frollo.frollosdksample.base

import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import us.frollo.frollosdk.model.IAdapterModel
import us.frollo.frollosdksample.utils.inflate

abstract class BaseRecyclerAdapter<T: IAdapterModel, VH : BaseViewHolder<T>>(klass: Class<T>, private val comparator: Comparator<T>? = null) : RecyclerView.Adapter<VH>() {
    protected var mData = listOf<T>()
    protected var mClickCallback: ((T, View, Int) -> Unit)? = null
    protected var mLongClickCallback: ((T, View, Int) -> Unit)? = null

    fun onItemClick(l: (model: T?, v: View?, p: Int) -> Unit) {
        this.mClickCallback = l
        notifyDataSetChanged()
    }

    fun onItemLongClick(l: (model: T?, v: View?, p: Int) -> Unit) {
        mLongClickCallback = l
        notifyDataSetChanged()
    }

    fun replaceAll(data: List<T>) {
        mData = comparator?.let { data.sortedWith(comparator) } ?: run { data }
        notifyDataSetChanged()
    }

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
		val viewHolder: VH = getViewHolder(parent.inflate(getViewHolderLayout(viewType)), viewType)
		mClickCallback?.let { viewHolder.setOnClickListener(this::invokeClickCallback) }
        mLongClickCallback?.let { viewHolder.setOnLongClickListener(this::invokeLongClickCallback) }
		return viewHolder
	}

    override fun onBindViewHolder(holder: VH, position: Int) {
        mData[position].let { holder.bind(it) }
    }

    override fun onViewRecycled(holder: VH) {
        super.onViewRecycled(holder)
        holder.recycle()
    }

    override fun getItemCount(): Int = mData.size

    protected open fun invokeClickCallback(view: View, pos: Int) {
        if (pos < mData.size && pos >= 0) this.mClickCallback?.invoke(mData[pos], view, pos)
    }

    protected open fun invokeLongClickCallback(view: View, pos: Int) {
        if (pos < mData.size && pos >= 0) this.mLongClickCallback?.invoke(mData[pos], view, pos)
    }

    @LayoutRes
    protected abstract fun getViewHolderLayout(viewType: Int): Int
    protected abstract fun getViewHolder(view: View, viewType: Int): VH
}