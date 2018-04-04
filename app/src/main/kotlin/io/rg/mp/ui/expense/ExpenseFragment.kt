package io.rg.mp.ui.expense

import android.app.Activity.RESULT_OK
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.AdapterView
import android.widget.DatePicker
import android.widget.Toast
import dagger.android.support.AndroidSupportInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.rg.mp.R
import io.rg.mp.drive.data.Balance
import io.rg.mp.persistence.entity.Category
import io.rg.mp.ui.expense.ExpenseViewModel.Companion.REQUEST_AUTHORIZATION_EXPENSE
import io.rg.mp.ui.expense.ExpenseViewModel.Companion.REQUEST_AUTHORIZATION_LOADING_ALL
import io.rg.mp.ui.expense.ExpenseViewModel.Companion.REQUEST_AUTHORIZATION_LOADING_CATEGORIES
import io.rg.mp.ui.expense.ExpenseViewModel.Companion.REQUEST_AUTHORIZATION_NEW_SPREADSHEET
import io.rg.mp.ui.expense.adapter.CategorySpinnerAdapter
import io.rg.mp.ui.expense.adapter.SpreadsheetSpinnerAdapter
import io.rg.mp.ui.expense.model.DateInt
import io.rg.mp.ui.extension.setVisibility
import io.rg.mp.ui.model.BalanceUpdated
import io.rg.mp.ui.model.DateChanged
import io.rg.mp.ui.model.ListCategory
import io.rg.mp.ui.model.ListSpreadsheet
import io.rg.mp.ui.model.SavedSuccessfully
import io.rg.mp.ui.model.StartActivity
import io.rg.mp.ui.model.ToastInfo
import io.rg.mp.ui.model.ViewModelResult
import kotlinx.android.synthetic.main.fragment_expense.*
import javax.inject.Inject


class ExpenseFragment : Fragment(), DatePickerDialog.OnDateSetListener {
    companion object {
        private const val LAST_DATE_KEY = "io.rg.mp.LAST_DATE_KEY"
    }

    @Inject
    lateinit var viewModel: ExpenseViewModel

    private lateinit var categorySpinnerAdapter: CategorySpinnerAdapter
    private lateinit var spreadsheetSpinnerAdapter: SpreadsheetSpinnerAdapter
    private lateinit var datePickerDialog: DatePickerDialog

    private val compositeDisposable = CompositeDisposable()

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        categorySpinnerAdapter = CategorySpinnerAdapter(
                activity, android.R.layout.simple_spinner_dropdown_item, activity.layoutInflater)

        spreadsheetSpinnerAdapter = SpreadsheetSpinnerAdapter(
                activity, android.R.layout.simple_spinner_dropdown_item, activity.layoutInflater)

        datePickerDialog = DatePickerDialog(activity, this, 0, 0, 0)

        return inflater!!.inflate(R.layout.fragment_expense, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        categorySpinner.adapter = categorySpinnerAdapter

        spreadsheetSpinner.adapter = spreadsheetSpinnerAdapter
        spreadsheetSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, v: View, pos: Int, id: Long) {
                val spreadsheet = spreadsheetSpinnerAdapter.getItem(pos)
                viewModel.onSpreadsheetItemSelected(spreadsheet.id)
            }

            override fun onNothingSelected(parent: AdapterView<out Adapter>?) {}
        }

        addButton.setOnClickListener { saveExpense() }

        val dateToUpdate = savedInstanceState?.getParcelable<DateInt>(LAST_DATE_KEY)
                ?: viewModel.lastDate()
        viewModel.updateDate(dateToUpdate)

        dateButton.setOnClickListener {
            val (year, month, dayOfWeek) = viewModel.lastDate()

            datePickerDialog.updateDate(year, month, dayOfWeek)
            datePickerDialog.show()
        }
    }

    private fun saveExpense() {
        val amount = amountEditText.text.toString().toFloat()
        val category = categorySpinner.selectedItem as Category
        val description = descriptionEditText.text.toString()

        viewModel.saveExpense(amount, category, description)
    }

    override fun onStart() {
        compositeDisposable.add(
                viewModel.viewModelNotifier()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(handleViewModelResult())
        )
        compositeDisposable.add(
                viewModel.isOperationInProgress()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(handleProgressBar())
        )

        viewModel.loadData()
        super.onStart()
    }

    private fun handleViewModelResult(): (ViewModelResult) -> Unit {
        return {
            when (it) {
                is ToastInfo -> Toast.makeText(activity, it.messageId, it.length).show()
                is StartActivity -> startActivityForResult(it.intent, it.requestCode)
                is ListCategory -> categorySpinnerAdapter.setItems(it.list)
                is ListSpreadsheet -> {
                    spreadsheetSpinnerAdapter.setItems(it.list)
                    spreadsheetSpinner.setSelection(viewModel.currentSpreadsheet(it.list))
                }
                is SavedSuccessfully -> {
                    amountEditText.text.clear()
                    descriptionEditText.text.clear()
                }
                is DateChanged -> dateButton.text = it.date
                is BalanceUpdated -> updateBalance(it.balance)
            }

        }
    }

    private fun handleProgressBar(): (Boolean) -> Unit {
        return { showProgress ->
            progressBar.isIndeterminate = showProgress

            progressBar.setVisibility(showProgress)
            actualBalanceLabel.setVisibility(!showProgress)
            currentBalanceLabel.setVisibility(!showProgress)
            plannedBalanceLabel.setVisibility(!showProgress)
            actualBalanceTextView.setVisibility(!showProgress)
            currentBalanceTextView.setVisibility(!showProgress)
            plannedBalanceTextView.setVisibility(!showProgress)
        }
    }

    private fun updateBalance(balance: Balance) {
        currentBalanceTextView.text = balance.current
        actualBalanceTextView.text = balance.actual
        plannedBalanceTextView.text = balance.planned
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        outState?.apply {
            putParcelable(LAST_DATE_KEY, viewModel.lastDate())
        }
    }

    override fun onStop() {
        super.onStop()
        compositeDisposable.clear()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_AUTHORIZATION_EXPENSE -> saveExpense()
                REQUEST_AUTHORIZATION_LOADING_ALL -> viewModel.loadData()
                REQUEST_AUTHORIZATION_LOADING_CATEGORIES -> viewModel.loadCurrentCategories()
                REQUEST_AUTHORIZATION_NEW_SPREADSHEET -> viewModel.createNewSpreadsheet()
            }
        }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        viewModel.updateDate(DateInt(year, month, dayOfMonth))
    }
}