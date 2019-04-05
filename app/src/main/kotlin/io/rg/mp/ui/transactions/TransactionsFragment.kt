package io.rg.mp.ui.transactions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import dagger.android.support.AndroidSupportInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.rg.mp.R
import io.rg.mp.ui.ListTransaction
import io.rg.mp.ui.ViewModelResult
import io.rg.mp.utils.setVisibility
import kotlinx.android.synthetic.main.fragment_transactions.transactionRecyclerView
import javax.inject.Inject

class TransactionsFragment : Fragment() {
    companion object {
        private const val SPREADSHEET_ID = "spreadsheetId"

        fun createArgs(spreadsheetId: String): Bundle {
            val args = Bundle()
            args.putString(SPREADSHEET_ID, spreadsheetId)

            return args
        }
    }

    @Inject
    lateinit var viewModel: TransactionsViewModel

    private lateinit var mainProgressBar: ProgressBar

    private val compositeDisposable = CompositeDisposable()
    private val transactionAdapter = TransactionsAdapter()

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    private lateinit var spreadsheetId: String

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        requireActivity().title = getString(R.string.transactions_title)

        spreadsheetId = arguments?.getString(SPREADSHEET_ID) ?: ""

        return inflater.inflate(R.layout.fragment_transactions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainProgressBar = requireActivity().findViewById(R.id.mainProgressBar)

        transactionRecyclerView.adapter = transactionAdapter
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

        viewModel.loadData(spreadsheetId)

        super.onStart()
    }

    private fun handleViewModelResult(): (ViewModelResult) -> Unit {
        return {
            when (it) {
                is ListTransaction -> transactionAdapter.setData(it.list)
            }
        }
    }

    private fun handleProgressBar(): (Boolean) -> Unit {
        return { isInProgress ->
            mainProgressBar.isIndeterminate = isInProgress
            mainProgressBar.setVisibility(isInProgress)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                TransactionsViewModel.REQUEST_AUTHORIZATION_DOWNLOADING_TRANSACTIONS -> viewModel.loadData(spreadsheetId)
            }
        }
    }
}