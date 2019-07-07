package io.rg.mp.ui.categories

import android.widget.Toast
import io.reactivex.schedulers.Schedulers
import io.rg.mp.R
import io.rg.mp.drive.CategoryService
import io.rg.mp.drive.data.NotSaved
import io.rg.mp.drive.data.Result
import io.rg.mp.drive.data.Saved
import io.rg.mp.persistence.dao.CategoryDao
import io.rg.mp.persistence.entity.Category
import io.rg.mp.ui.AbstractViewModel
import io.rg.mp.ui.ListCategory
import io.rg.mp.ui.SavedSuccessfully
import io.rg.mp.ui.ToastInfo

class CategoriesViewModel(
        private val categoryDao: CategoryDao,
        private val categoryService: CategoryService) : AbstractViewModel() {
    companion object {
        const val REQUEST_AUTHORIZATION_LOADING_CATEGORIES = 5001
        const val REQUEST_AUTHORIZATION_UPDATING_CATEGORY = 5002
        const val REQUEST_AUTHORIZATION_CREATING_CATEGORY = 5003
    }

    fun reloadData(spreadsheetId: String) {
        reloadCategories(spreadsheetId)
        downloadCategories(spreadsheetId)
    }

    private fun reloadCategories(spreadsheetId: String) {
        val disposable = categoryDao.findBySpreadsheetId(spreadsheetId)
                .subscribeOn(Schedulers.io())
                .subscribe { subject.onNext(ListCategory(it)) }
        compositeDisposable.add(disposable)
    }

    private fun downloadCategories(spreadsheetId: String) {
        val disposable = categoryService.getListBy(spreadsheetId)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnSubscribe { progressSubject.onNext(1) }
                .doFinally { progressSubject.onNext(-1) }
                .subscribe(
                        { categoryDao.insertAll(*it.list.toTypedArray()) },
                        { handleErrors(it, REQUEST_AUTHORIZATION_LOADING_CATEGORIES) }
                )
        compositeDisposable.add(disposable)
    }

    fun updatePlannedAmount(category: Category) {
        val disposable = categoryService.updateCategory(category)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(
                        { subject.onNext(SavedSuccessfully) },
                        { handleErrors(it,REQUEST_AUTHORIZATION_UPDATING_CATEGORY ) })
        compositeDisposable.add(disposable)
    }

    fun createCategory(spreadsheetId: String, name: String, amount: String) {
        val disposable = categoryService.createCategory(spreadsheetId, name, amount)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(
                        { handleCreating(it, spreadsheetId) },
                        { handleErrors(it, REQUEST_AUTHORIZATION_CREATING_CATEGORY) }
                )
        compositeDisposable.add(disposable)
    }

    private fun handleCreating(result: Result, spreadsheetId: String) {
        val messageId = when (result) {
            is Saved -> {
                subject.onNext(SavedSuccessfully)
                reloadData(spreadsheetId)
                R.string.saved_message
            }
            is NotSaved -> R.string.not_saved_message
        }

        subject.onNext(ToastInfo(messageId, Toast.LENGTH_LONG))
    }
}