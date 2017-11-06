package io.rg.mp.ui.auth

import dagger.Subcomponent
import dagger.android.AndroidInjector
import io.rg.mp.ui.FragmentScope


@Subcomponent(modules = arrayOf(AuthServiceModule::class))
@FragmentScope
interface AuthSubcomponent: AndroidInjector<AuthFragment> {
    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<AuthFragment>()
}