package io.rg.mp.ui.categories

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.BackpressureStrategy.BUFFER
import io.reactivex.Flowable
import io.reactivex.subjects.PublishSubject
import io.rg.mp.R
import io.rg.mp.persistence.entity.Category
import io.rg.mp.ui.categories.CategoriesAdapter.ViewHolder

class CategoriesAdapter : RecyclerView.Adapter<ViewHolder>() {

    private val categoryList: MutableList<Category> = mutableListOf()
    private val onClickSubject = PublishSubject.create<Category>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val cardLayout = LayoutInflater.from(parent.context)
                .inflate(R.layout.card_category, parent, false)

        return ViewHolder(cardLayout)
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categoryList[position]

        holder.itemView.setOnClickListener {
            onClickSubject.onNext(category)
        }

        holder.bindData(category)
    }

    fun setData(items: List<Category>) {
        categoryList.clear()
        categoryList.addAll(items)
        notifyDataSetChanged()
    }

    fun onClick(): Flowable<Category> = onClickSubject.toFlowable(BUFFER)

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        private val plannedTextView: TextView = itemView.findViewById(R.id.plannedTextView)
        private val spentMoneyTextView: TextView = itemView.findViewById(R.id.spentMoneyTextView)

        fun bindData(category: Category) {
            nameTextView.text = category.name
            plannedTextView.text = category.planned
            spentMoneyTextView.text = category.actual
        }
    }
}