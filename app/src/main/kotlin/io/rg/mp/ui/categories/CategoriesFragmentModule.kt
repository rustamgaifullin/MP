package io.rg.mp.ui.categories

import androidx.fragment.app.Fragment
import dagger.Binds
import dagger.Module
import dagger.android.AndroidInjector
import dagger.android.support.FragmentKey
import dagger.multibindings.IntoMap

@Module (subcomponents = [(CategoriesSubcomponent::class)])
abstract class CategoriesFragmentModule {
    @Binds
    @IntoMap
    @FragmentKey(CategoriesFragment::class)
    abstract fun bindCategoriesFragment(builder: CategoriesSubcomponent.Builder)
            : AndroidInjector.Factory<out Fragment>
}