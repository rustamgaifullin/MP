package io.rg.mp.ui.expense

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import dagger.android.support.AndroidSupportInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.rg.mp.R
import io.rg.mp.persistence.dao.CategoryDao
import io.rg.mp.persistence.dao.SpreadsheetDao
import io.rg.mp.persistence.entity.Category
import io.rg.mp.service.data.Expense
import io.rg.mp.service.data.NotSaved
import io.rg.mp.service.data.Saved
import io.rg.mp.service.drive.SpreadsheetService
import io.rg.mp.service.sheet.CategoryRetrieverService
import io.rg.mp.service.sheet.ExpenseService
import io.rg.mp.ui.expense.adapter.CategorySpinnerAdapter
import io.rg.mp.ui.expense.adapter.SpreadsheetSpinnerAdapter
import io.rg.mp.utils.Preferences
import io.rg.mp.utils.Toasts
import java.util.*
import javax.inject.Inject


class ExpenseFragment : Fragment() {
    @Inject lateinit var categoryService: CategoryRetrieverService
    @Inject lateinit var spreadsheetService: SpreadsheetService
    @Inject lateinit var expenseService: ExpenseService
    @Inject lateinit var categoryDao: CategoryDao
    @Inject lateinit var spreadsheetDao: SpreadsheetDao
    @Inject lateinit var toasts: Toasts
    @Inject lateinit var preferences: Preferences

    private lateinit var categorySpinner: Spinner
    private lateinit var categorySpinnerAdapter: CategorySpinnerAdapter

    private lateinit var spreadsheetSpinner: Spinner
    private lateinit var spreadsheetSpinnerAdapter: SpreadsheetSpinnerAdapter

    private lateinit var addButton: Button
    private lateinit var amountEditText: EditText

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
                preferences.spreadsheetId = spreadsheet.id
                reloadCategories()
                downloadCategories()
            }

            override fun onNothingSelected(parent: AdapterView<out Adapter>?) {}
        }

        amountEditText = view.findViewById(R.id.amount_edit_text)
        addButton = view.findViewById(R.id.add_button)

        addButton.setOnClickListener {
            val amount = amountEditText.text.toString().toFloat()
            val date = Date()
            val category = categorySpinner.selectedItem as Category
            val description = ""

            val expense = Expense(date, amount, description, category)

            expenseService.save(expense, preferences.spreadsheetId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        when(it) {
                            is Saved -> toasts.shortToast(activity, "Saved")
                            is NotSaved -> toasts.shortToast(activity, "Not saved")
                        }
                    }
        }

        return view
    }

    override fun onStart() {
        reloadSpreadsheets()
        reloadCategories()

        downloadSpreadsheets()
        if (preferences.isSpreadsheetIdAvailable) {
            downloadCategories()
        }

        super.onStart()
    }

    private fun reloadCategories() {
        categoryDao.findBySpreadsheetId(preferences.spreadsheetId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    categorySpinnerAdapter.setItems(it)
                }
    }

    private fun reloadSpreadsheets() {
        spreadsheetDao.all()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    spreadsheetSpinnerAdapter.setItems(it)

                    val position = it.indexOfFirst{ (id) -> id == preferences.spreadsheetId }
                    spreadsheetSpinner.setSelection(position)
                }
    }

    private fun downloadSpreadsheets() {
        spreadsheetService.list()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe { (list) -> spreadsheetDao.insertAll(*list.toTypedArray()) }
    }

    private fun downloadCategories() {
        val spreadsheetId = preferences.spreadsheetId

        categoryService.all(spreadsheetId)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe { (list) -> categoryDao.insertAll(*list.toTypedArray()) }
    }
}