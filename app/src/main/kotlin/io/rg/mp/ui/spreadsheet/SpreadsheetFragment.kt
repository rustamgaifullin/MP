package io.rg.mp.ui.spreadsheet

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import dagger.android.support.AndroidSupportInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.rg.mp.R
import io.rg.mp.ui.ListSpreadsheet
import io.rg.mp.ui.StartActivity
import io.rg.mp.ui.ToastInfo
import io.rg.mp.ui.ViewModelResult
import io.rg.mp.ui.expense.ExpenseFragment
import io.rg.mp.ui.spreadsheet.SpreadsheetViewModel.Companion.REQUEST_AUTHORIZATION_LOADING_SPREADSHEETS
import io.rg.mp.ui.spreadsheet.SpreadsheetViewModel.Companion.REQUEST_AUTHORIZATION_NEW_SPREADSHEET
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
                REQUEST_AUTHORIZATION_NEW_SPREADSHEET -> viewModel.createNewSpreadsheet()
                REQUEST_AUTHORIZATION_LOADING_SPREADSHEETS -> viewModel.reloadData()
            }
        }
    }
}