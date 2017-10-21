package io.rg.mp.ui.main.adapter

import android.content.Context
import android.view.LayoutInflater
import io.rg.mp.persistence.entity.Category

class CategorySpinnerAdapter(context: Context, resourceId: Int, inflater: LayoutInflater)
    : AbstractSpinnerAdapter<Category>(context, resourceId, inflater) {

    override fun getNameAt(position: Int) = getItem(position).name
}