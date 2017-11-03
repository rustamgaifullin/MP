package io.rg.mp.ui.auth

import dagger.Subcomponent
import dagger.android.AndroidInjector


@Subcomponent(modules = arrayOf(AuthServiceModule::class))
interface AuthSubcomponent: AndroidInjector<AuthFragment> {
    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<AuthFragment>()
}