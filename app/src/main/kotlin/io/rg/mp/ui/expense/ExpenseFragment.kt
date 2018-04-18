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
import android.widget.DatePicker
import android.widget.Toast
import dagger.android.support.AndroidSupportInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.rg.mp.R
import io.rg.mp.drive.data.Balance
import io.rg.mp.persistence.entity.Category
import io.rg.mp.ui.BalanceUpdated
import io.rg.mp.ui.ListCategory
import io.rg.mp.ui.SavedSuccessfully
import io.rg.mp.ui.StartActivity
import io.rg.mp.ui.ToastInfo
import io.rg.mp.ui.ViewModelResult
import io.rg.mp.ui.expense.ExpenseViewModel.Companion.REQUEST_AUTHORIZATION_EXPENSE
import io.rg.mp.ui.expense.ExpenseViewModel.Companion.REQUEST_AUTHORIZATION_LOADING_CATEGORIES
import io.rg.mp.ui.expense.adapter.CategorySpinnerAdapter
import io.rg.mp.ui.expense.model.DateInt
import io.rg.mp.utils.formatDate
import io.rg.mp.utils.setVisibility
import kotlinx.android.synthetic.main.fragment_expense.*
import javax.inject.Inject


class ExpenseFragment : Fragment(), DatePickerDialog.OnDateSetListener {
    companion object {
        private const val LAST_DATE_KEY = "io.rg.mp.LAST_DATE_KEY"
        private const val SPREADSHEET_ID = "spreadsheetId"
        const val NAME = "EXPENSE_FRAGMENT"

        fun create(spreadsheetId: String): ExpenseFragment {
            val expenseFragment = ExpenseFragment()

            val args = Bundle()
            args.putString(SPREADSHEET_ID, spreadsheetId)
            expenseFragment.arguments = args

            return expenseFragment
        }
    }

    @Inject
    lateinit var viewModel: ExpenseViewModel

    private lateinit var categorySpinnerAdapter: CategorySpinnerAdapter
    private lateinit var datePickerDialog: DatePickerDialog
    private lateinit var spreadsheetId: String

    private var date: DateInt = DateInt.currentDateInt()

    private val compositeDisposable = CompositeDisposable()

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        activity?.apply {
            categorySpinnerAdapter = CategorySpinnerAdapter(
                    this, android.R.layout.simple_spinner_dropdown_item, layoutInflater)
        }

        spreadsheetId = arguments?.getString(SPREADSHEET_ID) ?: ""

        datePickerDialog = DatePickerDialog(activity, this, 0, 0, 0)

        return inflater.inflate(R.layout.fragment_expense, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        categorySpinner.adapter = categorySpinnerAdapter

        addButton.setOnClickListener { saveExpense() }

        savedInstanceState?.apply {
            date = getParcelable(LAST_DATE_KEY)
        }

        dateButton.setOnClickListener {
            val (year, month, dayOfWeek) = date

            datePickerDialog.updateDate(year, month, dayOfWeek)
            datePickerDialog.show()
        }

        formatDateButtonText()
    }

    private fun saveExpense() {
        //TODO: validation of required fields please
        val amount = amountEditText.text.toString().toFloat()
        val category = categorySpinner.selectedItem as Category
        val description = descriptionEditText.text.toString()

        viewModel.saveExpense(amount, category, description, spreadsheetId, date)
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

        viewModel.reloadData(spreadsheetId)
        super.onStart()
    }

    private fun handleViewModelResult(): (ViewModelResult) -> Unit {
        return {
            when (it) {
                is ToastInfo -> Toast.makeText(activity, it.messageId, it.length).show()
                is StartActivity -> startActivityForResult(it.intent, it.requestCode)
                is ListCategory -> categorySpinnerAdapter.setItems(it.list)
                is SavedSuccessfully -> {
                    amountEditText.text.clear()
                    descriptionEditText.text.clear()
                }
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putParcelable(LAST_DATE_KEY, date)
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
                REQUEST_AUTHORIZATION_LOADING_CATEGORIES ->
                    viewModel.reloadData(spreadsheetId)
            }
        }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        date = DateInt(year, month, dayOfMonth)
        formatDateButtonText()
    }

    private fun formatDateButtonText() {
        dateButton.text = formatDate(date)
    }
}