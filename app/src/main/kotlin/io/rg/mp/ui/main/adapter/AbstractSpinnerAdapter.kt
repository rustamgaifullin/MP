package io.rg.mp.ui.main.adapter

import android.R
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter

abstract class AbstractSpinnerAdapter<T>(
        context: Context,
        val resourceId: Int,
        val inflater: LayoutInflater,
        private var items: MutableList<T> = mutableListOf<T>())
    : ArrayAdapter<T>(context, resourceId, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return getNewView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return getNewView(position, convertView, parent)
    }

    private fun getNewView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val viewHolder: ViewHolder
        val newView: View

        if (convertView == null) {
            newView = inflater.inflate(resourceId, parent, false)
            viewHolder = ViewHolder()
            viewHolder.textView = newView.findViewById(R.id.text1)
            newView.tag = viewHolder
        } else {
            newView = convertView
            viewHolder = newView.tag as ViewHolder
        }

        viewHolder.textView.text = getNameAt(position)

        return newView
    }

    abstract fun getNameAt(position: Int): String

    fun setItems(data: List<T>) {
        this.items.clear()
        this.items.addAll(data)
        notifyDataSetChanged()
    }
}