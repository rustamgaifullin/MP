package io.rg.mp.ui.categories

import io.reactivex.schedulers.Schedulers
import io.rg.mp.drive.CategoryService
import io.rg.mp.persistence.dao.CategoryDao
import io.rg.mp.ui.AbstractViewModel
import io.rg.mp.ui.ListCategory

class CategoriesViewModel(
        private val categoryDao: CategoryDao,
        private val categoryService: CategoryService) : AbstractViewModel() {
    companion object {
        const val REQUEST_AUTHORIZATION_LOADING_CATEGORIES = 5001
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
}