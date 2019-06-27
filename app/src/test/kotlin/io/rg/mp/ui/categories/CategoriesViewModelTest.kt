package io.rg.mp.ui.categories

import android.content.Intent
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Flowable
import io.rg.mp.drive.CategoryService
import io.rg.mp.drive.data.CategoryList
import io.rg.mp.persistence.dao.CategoryDao
import io.rg.mp.persistence.entity.Category
import io.rg.mp.rule.TrampolineSchedulerRule
import io.rg.mp.ui.ListCategory
import io.rg.mp.ui.StartActivity
import io.rg.mp.ui.categories.CategoriesViewModel.Companion.REQUEST_AUTHORIZATION_LOADING_CATEGORIES
import org.junit.Rule
import org.junit.Test

class CategoriesViewModelTest {
    @get:Rule
    val trampolineSchedulerRule = TrampolineSchedulerRule()

    private val categoryService: CategoryService = mock()
    private val categoryDao: CategoryDao = mock()

    private fun viewModel() = CategoriesViewModel(categoryDao, categoryService)

    @Test
    fun `should handle authorization error for loading categories`() {
        val sut = viewModel()
        val spreadsheetId = "id"

        whenever(categoryDao.findBySpreadsheetId(spreadsheetId)).thenReturn(
                Flowable.empty()
        )

        whenever(categoryService.getListBy(spreadsheetId)).thenReturn(
                Flowable.error(userRecoverableAuthIoException())
        )

        val testSubscriber = sut.viewModelNotifier().test()

        sut.reloadData(spreadsheetId)

        testSubscriber
                .assertNoErrors()
                .assertValue {
                    it is StartActivity
                            && it.requestCode == REQUEST_AUTHORIZATION_LOADING_CATEGORIES
                }
                .assertNotComplete()
    }

    @Test
    fun `should load categories and insert in db`() {
        val sut = viewModel()
        val category = Category("name", "", "", "", 0, "id")
        val spreadsheetId = "id"

        whenever(categoryDao.findBySpreadsheetId(spreadsheetId)).thenReturn(
                Flowable.just(listOf(category))
        )

        whenever(categoryService.getListBy(spreadsheetId)).thenReturn(
                Flowable.just(CategoryList(listOf(category)))
        )

        val testSubscriber = sut.viewModelNotifier().test()

        sut.reloadData(spreadsheetId)

        testSubscriber
                .assertNoErrors()
                .assertValue {
                    it is ListCategory && it.list[0] == category
                }
                .assertNotComplete()

        verify(categoryDao).insertAll(category)
    }

    private fun userRecoverableAuthIoException(): UserRecoverableAuthIOException {
        val wrapper = UserRecoverableAuthException("", Intent())
        return UserRecoverableAuthIOException(wrapper)
    }
}