package io.rg.mp.ui.expense.adapter

import android.content.Context
import android.view.LayoutInflater
import io.rg.mp.persistence.entity.Spreadsheet

class SpreadsheetSpinnerAdapter(context: Context, resourceId: Int, inflater: LayoutInflater)
    : AbstractSpinnerAdapter<Spreadsheet>(context, resourceId, inflater) {

    override fun getNameAt(position: Int) = getItem(position).name
}