package io.rg.mp.ui.categories

import dagger.Subcomponent
import dagger.android.AndroidInjector
import io.rg.mp.ui.FragmentScope

@Subcomponent(modules = [(CategoriesServiceModule::class)])
@FragmentScope
interface CategoriesSubcomponent : AndroidInjector<CategoriesFragment> {
    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<CategoriesFragment>()
}