package io.rg.mp.ui.transactions

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.rg.mp.R
import io.rg.mp.persistence.entity.Transaction

class TransactionsAdapter : RecyclerView.Adapter<TransactionsAdapter.ViewHolder>() {
    private val transactionList: MutableList<Transaction> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val cardLayout = LayoutInflater.from(parent.context)
                .inflate(R.layout.card_transaction, parent, false)

        return ViewHolder(cardLayout)
    }

    override fun getItemCount(): Int {
        return transactionList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = transactionList[position]

        holder.bindData(transaction)
    }

    fun setData(list: List<Transaction>) {
        transactionList.clear()
        transactionList.addAll(list)
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        private val amountTextView: TextView = itemView.findViewById(R.id.amountTextView)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
        private val categoryTextView: TextView = itemView.findViewById(R.id.categoryTextView)

        fun bindData(transaction: Transaction) {
            dateTextView.text = transaction.date
            amountTextView.text = transaction.amount
            descriptionTextView.text = transaction.description
            categoryTextView.text = transaction.category
        }
    }
}