package io.rg.mp.ui.categories

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import dagger.android.support.AndroidSupportInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.rg.mp.R
import io.rg.mp.R.layout
import javax.inject.Inject

class CategoriesFragment : Fragment() {

    @Inject
    lateinit var viewModel: CategoriesViewModel

    private val compositeDisposable = CompositeDisposable()
    private val categoriesAdapter = CategoriesAdapter()

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)

        return inflater.inflate(layout.fragment_spreadsheets, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainProgressBar = requireActivity().findViewById(R.id.mainProgressBar)

        registerForContextMenu(spreadsheetsRecyclerView)

        categoriesRecyclerView.adapter = categoriesAdapter

        reloadViewAuthenticator.restoreState(savedInstanceState)
        spreadsheetData = savedInstanceState?.getParcelable(SPREADSHEET_DATA_KEY)
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

        reloadViewAuthenticator.startReload {
            viewModel.reloadData()
        }

        super.onStart()
    }

    override fun onStop() {
        super.onStop()
        compositeDisposable.clear()
        viewModel.clear()
    }
}