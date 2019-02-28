package io.rg.mp.ui.spreadsheet

import androidx.recyclerview.widget.RecyclerView
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.PublishSubject
import io.rg.mp.R
import io.rg.mp.persistence.entity.Spreadsheet
import java.util.Date

class SpreadsheetAdapter : RecyclerView.Adapter<SpreadsheetAdapter.ViewHolder> () {
    private val spreadsheetList: MutableList<Spreadsheet> = mutableListOf()
    private val onClickSubject = PublishSubject.create<Spreadsheet>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val cardLayout = LayoutInflater.from(parent.context)
                .inflate(R.layout.card_spreadsheet, parent, false)

        return ViewHolder(cardLayout, DateFormat.getLongDateFormat(parent.context))
    }

    override fun getItemCount(): Int {
        return spreadsheetList.size
    }

    override fun onBindViewHolder(holder: SpreadsheetAdapter.ViewHolder, position: Int) {
        val spreadsheet = spreadsheetList[position]

        holder.bindData(spreadsheet)
        holder.itemView.setOnClickListener {
            onClickSubject.onNext(spreadsheet)
        }
    }

    fun setData(items: List<Spreadsheet>) {
        spreadsheetList.clear()
        spreadsheetList.addAll(items)
        notifyDataSetChanged()
    }

    fun onClick(): Flowable<Spreadsheet> = onClickSubject.toFlowable(BackpressureStrategy.BUFFER)

    class ViewHolder(
            itemView: View,
            private val dateFormat: java.text.DateFormat) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        private val lastModifiedTextView: TextView = itemView.findViewById(R.id.lastModifiedTextView)

        fun bindData(spreadsheet: Spreadsheet) {
            nameTextView.text = spreadsheet.name
            lastModifiedTextView.text = dateFormat.format(Date(spreadsheet.modifiedTime))
        }
    }
}