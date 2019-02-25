package io.rg.mp.ui.transactions

import dagger.Subcomponent
import dagger.android.AndroidInjector
import io.rg.mp.ui.FragmentScope

@Subcomponent(modules = [(TransactionsServiceModule::class)])
@FragmentScope
interface TransactionsSubcomponent : AndroidInjector<TransactionsFragment> {
    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<TransactionsFragment>()
}