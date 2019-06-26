package io.rg.mp.ui.categories

import com.google.api.services.sheets.v4.Sheets
import dagger.Module
import dagger.Provides
import io.rg.mp.drive.CategoryService
import io.rg.mp.persistence.dao.CategoryDao
import io.rg.mp.ui.FragmentScope
import io.rg.mp.ui.ReloadViewAuthenticator

@Module
class CategoriesServiceModule {
    @Provides
    @FragmentScope
    fun categoryService(sheets: Sheets) = CategoryService(sheets)

    @Provides
    @FragmentScope
    fun reloadViewAuthenticator() = ReloadViewAuthenticator()

    @Provides
    @FragmentScope
    fun categoriesViewModel(
            categoryDao: CategoryDao,
            categoryService: CategoryService): CategoriesViewModel {
        return CategoriesViewModel(categoryDao, categoryService)
    }
}