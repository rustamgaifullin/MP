package io.rg.mp.ui.main

import dagger.Subcomponent
import dagger.android.AndroidInjector


@Subcomponent(modules = arrayOf(MainServiceModule::class))
interface MainSubcomponent : AndroidInjector<MainFragment> {
    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<MainFragment>()
}