package io.rg.mp.ui.spreadsheet

import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnCreateContextMenuListener
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.BackpressureStrategy.BUFFER
import io.reactivex.Flowable
import io.reactivex.subjects.PublishSubject
import io.rg.mp.R
import io.rg.mp.persistence.entity.Spreadsheet
import io.rg.mp.ui.spreadsheet.SpreadsheetAdapter.ViewHolder

class SpreadsheetAdapter : RecyclerView.Adapter<ViewHolder>() {
    companion object {
        const val RENAME_ACTION = 10001
        const val COPY_ACTION = 10002
    }

    private val spreadsheetList: MutableList<Spreadsheet> = mutableListOf()
    private val onClickSubject = PublishSubject.create<SpreadsheetEvent>()

    var position = -1
        private set

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val cardLayout = LayoutInflater.from(parent.context)
                .inflate(R.layout.card_spreadsheet, parent, false)

        return ViewHolder(cardLayout)
    }

    override fun getItemCount(): Int {
        return spreadsheetList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val spreadsheet = spreadsheetList[position]

        holder.bindData(spreadsheet)
        holder.itemView.setOnClickListener {
            onClickSubject.onNext(SpreadsheetEvent(spreadsheet, holder.nameTextView))
        }

        holder.itemView.setOnLongClickListener {
            this.position = position
            false
        }
    }

    override fun onViewRecycled(holder: ViewHolder) {
        holder.itemView.setOnClickListener(null)
        holder.itemView.setOnLongClickListener(null)
        super.onViewRecycled(holder)
    }

    fun setData(items: List<Spreadsheet>) {
        spreadsheetList.clear()
        spreadsheetList.addAll(items)
        notifyDataSetChanged()
    }

    fun lastClicked():Spreadsheet? {
        if (position > -1 && spreadsheetList.isNotEmpty()) {
            return spreadsheetList[position]
        }

        return null
    }

    fun onClick(): Flowable<SpreadsheetEvent> = onClickSubject.toFlowable(BUFFER)

    class ViewHolder(
            itemView: View) : RecyclerView.ViewHolder(itemView), OnCreateContextMenuListener {
        init {
            itemView.setOnCreateContextMenuListener(this)
        }

        override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenuInfo?) {
            menu?.add(0, RENAME_ACTION, 0, "Rename")
            menu?.add(0, COPY_ACTION, 0, "Copy")
        }

        internal val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)

        fun bindData(spreadsheet: Spreadsheet) {
            nameTextView.text = spreadsheet.name
        }
    }
}

data class SpreadsheetEvent(val spreadsheet: Spreadsheet, val view: View)