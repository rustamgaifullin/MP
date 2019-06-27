package io.rg.mp.ui.categories

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog.Builder
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import dagger.android.support.AndroidSupportInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.rg.mp.R
import io.rg.mp.R.layout
import io.rg.mp.R.string
import io.rg.mp.persistence.entity.Category
import io.rg.mp.ui.ListCategory
import io.rg.mp.ui.ReloadViewAuthenticator
import io.rg.mp.ui.SavedSuccessfully
import io.rg.mp.ui.StartActivity
import io.rg.mp.ui.ToastInfo
import io.rg.mp.ui.ViewModelResult
import io.rg.mp.ui.categories.CategoriesViewModel.Companion.REQUEST_AUTHORIZATION_LOADING_CATEGORIES
import io.rg.mp.utils.setVisibility
import kotlinx.android.synthetic.main.fragment_categories.categoriesRecyclerView
import me.zhanghai.android.materialprogressbar.MaterialProgressBar
import javax.inject.Inject

class CategoriesFragment : Fragment() {
    companion object {
        private const val SPREADSHEET_ID = "io.rg.mp.SPREADSHEET_ID"

        fun createArgs(spreadsheetId: String): Bundle {
            val args = Bundle()
            args.putString(SPREADSHEET_ID, spreadsheetId)

            return args
        }
    }

    @Inject
    lateinit var viewModel: CategoriesViewModel

    @Inject
    lateinit var reloadViewAuthenticator: ReloadViewAuthenticator

    private val compositeDisposable = CompositeDisposable()
    private val categoriesAdapter = CategoriesAdapter()

    private lateinit var mainProgressBar: MaterialProgressBar
    private lateinit var spreadsheetId: String

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        spreadsheetId = arguments?.getString(SPREADSHEET_ID) ?: ""

        return inflater.inflate(layout.fragment_categories, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainProgressBar = requireActivity().findViewById(R.id.mainProgressBar)

        registerForContextMenu(categoriesRecyclerView)

        categoriesRecyclerView.adapter = categoriesAdapter

        reloadViewAuthenticator.restoreState(savedInstanceState)
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
                categoriesAdapter.onClick()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { editPlannedAmount(it) }
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
                is ListCategory -> categoriesAdapter.setData(it.list)
                is SavedSuccessfully -> viewModel.reloadData(spreadsheetId)
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        reloadViewAuthenticator.authenticationFinished()

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_AUTHORIZATION_LOADING_CATEGORIES -> viewModel.reloadData(spreadsheetId)
//                REQUEST_AUTHORIZATION_UPDATING_CATEGORY -> viewModel.updatePlannedAmount() TODO: need to store category somewhere
            }
        }
    }

    private fun handleProgressBar(): (Boolean) -> Unit {
        return { isInProgress ->
            mainProgressBar.isIndeterminate = isInProgress
            mainProgressBar.setVisibility(isInProgress)
        }
    }

    private fun editPlannedAmount(category: Category) {
        val builder = Builder(requireActivity())
        builder.setTitle(getString(string.enter_name))

        //TODO: add validator for the editText
        val view = View.inflate(requireContext(), layout.dialog_edittext, null)
        val editText = view.findViewById<TextInputEditText>(R.id.dialogEditText)
        editText.setText(category.planned)

        builder.setView(view)

        builder.setNegativeButton(android.R.string.cancel) { dialog, _ ->
            dialog.dismiss()
        }

        builder.setPositiveButton(getString(string.update)) { dialog, _ ->
            category.planned = editText.text.toString()
            viewModel.updatePlannedAmount(category)
            dialog.dismiss()
        }

        builder.create().show()
    }

    override fun onStop() {
        super.onStop()
        compositeDisposable.clear()
        viewModel.clear()
    }
}