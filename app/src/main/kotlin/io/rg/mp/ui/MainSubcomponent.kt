package io.rg.mp.ui

import dagger.Subcomponent
import dagger.android.AndroidInjector


@Subcomponent
interface MainSubcomponent : AndroidInjector<MainActivity> {
    @Subcomponent.Builder
    abstract class Builder: AndroidInjector.Builder<MainActivity>()
}