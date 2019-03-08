package io.rg.mp.ui.spreadsheet

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.PublishSubject
import io.rg.mp.R
import io.rg.mp.persistence.entity.Spreadsheet

class SpreadsheetAdapter : RecyclerView.Adapter<SpreadsheetAdapter.ViewHolder> () {
    private val spreadsheetList: MutableList<Spreadsheet> = mutableListOf()
    private val onClickSubject = PublishSubject.create<SpreadsheetEvent>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val cardLayout = LayoutInflater.from(parent.context)
                .inflate(R.layout.card_spreadsheet, parent, false)

        return ViewHolder(cardLayout)
    }

    override fun getItemCount(): Int {
        return spreadsheetList.size
    }

    override fun onBindViewHolder(holder: SpreadsheetAdapter.ViewHolder, position: Int) {
        val spreadsheet = spreadsheetList[position]

        holder.bindData(spreadsheet)
        holder.itemView.setOnClickListener {
            onClickSubject.onNext(SpreadsheetEvent(spreadsheet, holder.nameTextView))
        }
    }

    fun setData(items: List<Spreadsheet>) {
        spreadsheetList.clear()
        spreadsheetList.addAll(items)
        notifyDataSetChanged()
    }

    fun onClick(): Flowable<SpreadsheetEvent> = onClickSubject.toFlowable(BackpressureStrategy.BUFFER)

    class ViewHolder(
            itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)

        fun bindData(spreadsheet: Spreadsheet) {
            nameTextView.text = spreadsheet.name
        }
    }
}

data class SpreadsheetEvent(val spreadsheet: Spreadsheet, val view: View)