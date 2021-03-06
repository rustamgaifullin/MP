package io.rg.mp.ui.expense

import android.app.Activity.RESULT_OK
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import dagger.android.support.AndroidSupportInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.rg.mp.R
import io.rg.mp.R.string
import io.rg.mp.drive.data.Balance
import io.rg.mp.persistence.entity.Category
import io.rg.mp.ui.ListCategory
import io.rg.mp.ui.ReloadViewAuthenticator
import io.rg.mp.ui.SavedSuccessfully
import io.rg.mp.ui.SpreadsheetData
import io.rg.mp.ui.StartActivity
import io.rg.mp.ui.ToastInfo
import io.rg.mp.ui.ViewModelResult
import io.rg.mp.ui.expense.ExpenseViewModel.Companion.REQUEST_AUTHORIZATION_EXPENSE
import io.rg.mp.ui.expense.ExpenseViewModel.Companion.REQUEST_AUTHORIZATION_LOADING
import io.rg.mp.ui.expense.model.DateInt
import io.rg.mp.ui.transactions.TransactionsFragment
import io.rg.mp.utils.formatDate
import io.rg.mp.utils.setVisibility
import kotlinx.android.synthetic.main.fragment_expense.actualBalanceTextView
import kotlinx.android.synthetic.main.fragment_expense.addButton
import kotlinx.android.synthetic.main.fragment_expense.amountEditText
import kotlinx.android.synthetic.main.fragment_expense.amountTextInputLayout
import kotlinx.android.synthetic.main.fragment_expense.categoryEditText
import kotlinx.android.synthetic.main.fragment_expense.currentBalanceTextView
import kotlinx.android.synthetic.main.fragment_expense.dateEditText
import kotlinx.android.synthetic.main.fragment_expense.descriptionEditText
import kotlinx.android.synthetic.main.fragment_expense.plannedBalanceTextView
import kotlinx.android.synthetic.main.fragment_expense.titleTextView
import me.zhanghai.android.materialprogressbar.MaterialProgressBar
import javax.inject.Inject


class ExpenseFragment : Fragment(), DatePickerDialog.OnDateSetListener {
    companion object {
        private const val LAST_DATE_KEY = "io.rg.mp.LAST_DATE_KEY"
        private const val SPREADSHEET_NAME = "io.rg.mp.SPREADSHEET_NAME"
        private const val SPREADSHEET_ID = "io.rg.mp.SPREADSHEET_ID"

        fun createArgs(spreadsheetId: String, spreadsheetName: String): Bundle {
            val args = Bundle()
            args.putString(SPREADSHEET_ID, spreadsheetId)
            args.putString(SPREADSHEET_NAME, spreadsheetName)

            return args
        }
    }

    @Inject
    lateinit var viewModel: ExpenseViewModel

    @Inject
    lateinit var reloadViewAuthenticator: ReloadViewAuthenticator

    private lateinit var datePickerDialog: DatePickerDialog
    private lateinit var spreadsheetId: String
    private lateinit var mainProgressBar: MaterialProgressBar

    private var date: DateInt = DateInt.currentDateInt()

    private val compositeDisposable = CompositeDisposable()

    private var categories: List<Category> = emptyList()

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        spreadsheetId = arguments?.getString(SPREADSHEET_ID) ?: ""

        requireActivity().title = getString(R.string.expenses_title)

        datePickerDialog = DatePickerDialog(requireContext(), this, 0, 0, 0)

        setHasOptionsMenu(true)

        return inflater.inflate(R.layout.fragment_expense, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainProgressBar = requireActivity().findViewById(R.id.mainProgressBar)

        addButton.setOnClickListener { validateAndSaveExpense() }

        savedInstanceState?.apply {
            date = getParcelable(LAST_DATE_KEY) ?: DateInt.currentDateInt()
        }

        titleTextView.text = arguments?.getString(SPREADSHEET_NAME) ?: ""

        dateEditText.setOnClickListener {
            val (year, month, dayOfWeek) = date

            datePickerDialog.updateDate(year, month, dayOfWeek)
            datePickerDialog.show()
        }

        categoryEditText.setOnClickListener {
            val categoryNames = categories.map { it.name }
            val indexOfFirst = categoryNames.indexOfFirst { it == categoryEditText.text?.toString() }

            val builder = AlertDialog.Builder(requireActivity())
            builder.setTitle(getString(R.string.choose_category))

            builder.setSingleChoiceItems(categoryNames.toTypedArray(), indexOfFirst) { dialog, index ->
                categoryEditText.setText(categoryNames[index])
                dialog.dismiss()
            }

            builder.setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }

            builder.create().show()
        }

        amountEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s?.isNotEmpty() == true) {
                    setAmountTextInputLayoutErrorMessage(false)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        formatDateButtonText()

        reloadViewAuthenticator.restoreState(savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.menu_expense, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_transactions -> openTransactions()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun openTransactions() {
        val args = TransactionsFragment.createArgs(spreadsheetId)

        view?.findNavController()?.navigate(R.id.actionShowTransactionsScreen, args)
    }

    private fun validateAndSaveExpense() {
        if (amountEditText.text.isNullOrEmpty()) {
            setAmountTextInputLayoutErrorMessage(true)
        } else {
            saveExpense()
        }
    }

    private fun setAmountTextInputLayoutErrorMessage(isTextEmpty: Boolean) {
        if (isTextEmpty) {
            amountTextInputLayout.error = getString(string.amount_error_message)
        } else {
            amountTextInputLayout.error = null
        }

        amountTextInputLayout.isErrorEnabled = isTextEmpty
    }

    private fun saveExpense() {
        val amount = amountEditText.text.toString().toFloat()
        val category = categories.first { it.name == categoryEditText.text?.toString() }
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

        reloadViewAuthenticator.startReload {
            viewModel.reloadData(spreadsheetId)
        }

        super.onStart()
    }

    private fun handleViewModelResult(): (ViewModelResult) -> Unit {
        return {
            when (it) {
                is ToastInfo -> Toast.makeText(activity, it.messageId, it.length).show()
                is StartActivity -> reloadViewAuthenticator.startAuthentication {
                    startActivityForResult(it.intent, it.requestCode)
                }
                is ListCategory -> {
                    categories = it.list
                    categoryEditText.setText(categories.getOrNull(0)?.name)
                }
                is SavedSuccessfully -> {
                    amountEditText.text?.clear()
                    descriptionEditText.text?.clear()
                }
                is SpreadsheetData -> updateBalance(Balance.fromSpreadsheet(it.spreadsheet))
            }

        }
    }

    private fun handleProgressBar(): (Boolean) -> Unit {
        return { isInProgress ->
            mainProgressBar.isIndeterminate = isInProgress
            mainProgressBar.setVisibility(isInProgress)
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
        outState.putAll(reloadViewAuthenticator.getState())
    }

    override fun onStop() {
        super.onStop()
        compositeDisposable.clear()
        viewModel.clear()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        reloadViewAuthenticator.authenticationFinished()

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_AUTHORIZATION_EXPENSE -> validateAndSaveExpense()
                REQUEST_AUTHORIZATION_LOADING -> viewModel.reloadData(spreadsheetId)
            }
        }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        date = DateInt(year, month, dayOfMonth)
        formatDateButtonText()
    }

    private fun formatDateButtonText() {
        dateEditText.setText(formatDate(date))
    }
}