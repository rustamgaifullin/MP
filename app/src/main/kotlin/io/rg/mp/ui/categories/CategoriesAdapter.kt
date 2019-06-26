package io.rg.mp.ui.categories

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.rg.mp.R
import io.rg.mp.persistence.entity.Category
import io.rg.mp.ui.categories.CategoriesAdapter.ViewHolder

class CategoriesAdapter: RecyclerView.Adapter<ViewHolder>() {

    private val categoryList: MutableList<Category> = mutableListOf()

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

        holder.bindData(category)
    }

    fun setData(items: List<Category>) {
        categoryList.clear()
        categoryList.addAll(items)
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)

        fun bindData(category: Category) {
            nameTextView.text = category.name
        }
    }
}