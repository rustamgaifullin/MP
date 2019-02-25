package io.rg.mp.ui.transactions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.android.support.AndroidSupportInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.rg.mp.R
import io.rg.mp.ui.ListTransaction
import io.rg.mp.ui.ViewModelResult
import kotlinx.android.synthetic.main.fragment_transactions.*
import javax.inject.Inject

class TransactionsFragment : Fragment() {
    companion object {
        const val NAME = "TRANSACTIONS_FRAGMENT"

        private const val SPREADSHEET_ID = "spreadsheetId"

        fun create(spreadsheetId: String): TransactionsFragment {
            val transactionsFragment = TransactionsFragment()

            val args = Bundle()
            args.putString(SPREADSHEET_ID, spreadsheetId)
            transactionsFragment.arguments = args

            return transactionsFragment
        }
    }

    @Inject
    lateinit var viewModel: TransactionsViewModel

    private val compositeDisposable = CompositeDisposable()
    private val transactionAdapter = TransactionsAdapter()

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    private lateinit var spreadsheetId: String

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        requireActivity().title = getString(R.string.transactionsTitle)

        spreadsheetId = arguments?.getString(SPREADSHEET_ID) ?: ""

        return inflater.inflate(R.layout.fragment_transactions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

    private fun handleProgressBar(): (Boolean) -> Unit  {
        return {}
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