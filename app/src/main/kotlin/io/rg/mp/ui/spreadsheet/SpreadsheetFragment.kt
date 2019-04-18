package io.rg.mp.ui.spreadsheet

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.google.android.material.textfield.TextInputEditText
import dagger.android.support.AndroidSupportInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.rg.mp.R
import io.rg.mp.R.string
import io.rg.mp.ui.CreatedSuccessfully
import io.rg.mp.ui.ListSpreadsheet
import io.rg.mp.ui.StartActivity
import io.rg.mp.ui.ToastInfo
import io.rg.mp.ui.ViewModelResult
import io.rg.mp.ui.expense.ExpenseFragment
import io.rg.mp.ui.spreadsheet.SpreadsheetViewModel.Companion.REQUEST_AUTHORIZATION_LOADING_SPREADSHEETS
import io.rg.mp.ui.spreadsheet.SpreadsheetViewModel.Companion.REQUEST_AUTHORIZATION_NEW_SPREADSHEET
import io.rg.mp.ui.spreadsheet.SpreadsheetViewModel.Companion.SPREADSHEET_NAME
import kotlinx.android.synthetic.main.fragment_spreadsheets.spreadsheetsRecyclerView
import javax.inject.Inject

class SpreadsheetFragment : Fragment() {
    @Inject
    lateinit var viewModel: SpreadsheetViewModel

    private val compositeDisposable = CompositeDisposable()
    private val spreadsheetAdapter = SpreadsheetAdapter()

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)

        return inflater.inflate(R.layout.fragment_spreadsheets, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        spreadsheetsRecyclerView.adapter = spreadsheetAdapter
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
        compositeDisposable.add(
                spreadsheetAdapter.onClick()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(openExpenseFragment())
        )

        viewModel.reloadData()
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
        compositeDisposable.clear()
        viewModel.clear()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.menu_spreadsheet, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_create_spreadsheet -> createSpreadsheet()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun createSpreadsheet() {
        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle(getString(string.enter_name))

        val view = View.inflate(requireContext(), R.layout.dialog_edittext, null)
        val editText = view.findViewById<TextInputEditText>(R.id.dialogEditText)
        editText.setText(viewModel.createSpreadsheetName())

        builder.setView(view)

        builder.setNegativeButton(android.R.string.cancel) { dialog, _ ->
            dialog.dismiss()
        }

        builder.setPositiveButton(getString(string.create)) { dialog, _ ->
            viewModel.createNewSpreadsheet(editText.text.toString())
            dialog.dismiss()
        }

        builder.create().show()
    }

    private fun openExpenseFragment(): (SpreadsheetEvent) -> Unit {
        return {
            val args = ExpenseFragment.createArgs(it.spreadsheet.id, it.spreadsheet.name)

            view?.findNavController()?.navigate(R.id.actionShowExpenseScreen, args)
        }
    }

    private fun handleViewModelResult(): (ViewModelResult) -> Unit {
        return {
            when (it) {
                is ToastInfo -> Toast.makeText(activity, it.messageId, it.length).show()
                is StartActivity -> startActivityForResult(it.intent, it.requestCode)
                is ListSpreadsheet -> {
                    spreadsheetAdapter.setData(it.list)
                }
                is CreatedSuccessfully -> {
                    Toast.makeText(activity, "Created successfully", LENGTH_SHORT).show()
                    viewModel.reloadData()
                }
            }

        }
    }

    private fun handleProgressBar(): (Boolean) -> Unit {
        return {}
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_AUTHORIZATION_NEW_SPREADSHEET -> {
                    val name = data?.getStringExtra(SPREADSHEET_NAME) ?: ""
                    viewModel.createNewSpreadsheet(name)
                }
                REQUEST_AUTHORIZATION_LOADING_SPREADSHEETS -> viewModel.reloadData()
            }
        }
    }
}