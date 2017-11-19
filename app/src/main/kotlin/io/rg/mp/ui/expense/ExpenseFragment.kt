package io.rg.mp.ui.expense

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import dagger.android.support.AndroidSupportInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.rg.mp.R
import io.rg.mp.persistence.entity.Category
import io.rg.mp.ui.expense.adapter.CategorySpinnerAdapter
import io.rg.mp.ui.expense.adapter.SpreadsheetSpinnerAdapter
import javax.inject.Inject


class ExpenseFragment : Fragment() {
    @Inject lateinit var viewModel: ExpenseViewModel

    private lateinit var categorySpinner: Spinner
    private lateinit var categorySpinnerAdapter: CategorySpinnerAdapter

    private lateinit var spreadsheetSpinner: Spinner
    private lateinit var spreadsheetSpinnerAdapter: SpreadsheetSpinnerAdapter

    private lateinit var addButton: Button
    private lateinit var amountEditText: EditText

    private val compositeDisposable = CompositeDisposable()

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_main, container, false)

        categorySpinner = view.findViewById(R.id.category_spinner)
        categorySpinnerAdapter = CategorySpinnerAdapter(
                activity, android.R.layout.simple_spinner_dropdown_item, activity.layoutInflater)
        categorySpinner.adapter = categorySpinnerAdapter

        spreadsheetSpinner = view.findViewById(R.id.spreadsheet_spinner)
        spreadsheetSpinnerAdapter = SpreadsheetSpinnerAdapter(
                activity, android.R.layout.simple_spinner_dropdown_item, activity.layoutInflater)
        spreadsheetSpinner.adapter = spreadsheetSpinnerAdapter
        spreadsheetSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, v: View, pos: Int, id: Long) {
                val spreadsheet = spreadsheetSpinnerAdapter.getItem(pos)
                viewModel.onSpreadsheetItemSelected(spreadsheet.id)
            }

            override fun onNothingSelected(parent: AdapterView<out Adapter>?) {}
        }

        amountEditText = view.findViewById(R.id.amount_edit_text)
        addButton = view.findViewById(R.id.add_button)

        addButton.setOnClickListener {
            val amount = amountEditText.text.toString().toFloat()
            val category = categorySpinner.selectedItem as Category

            viewModel.saveExpense(amount, category)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        Toast.makeText(activity, it.messageId, it.length).show();
                    }
        }

        return view
    }

    override fun onStart() {
        compositeDisposable.add(viewModel.getSpreadsheets()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    spreadsheetSpinnerAdapter.setItems(it)
                    spreadsheetSpinner.setSelection(viewModel.currentSpreadsheet(it))
                })

        compositeDisposable.add(viewModel.getCategories()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    categorySpinnerAdapter.setItems(it)
                })

        viewModel.loadData()
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
        compositeDisposable.clear()
    }
}