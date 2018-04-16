package io.rg.mp.ui.expense

import dagger.Subcomponent
import dagger.android.AndroidInjector
import io.rg.mp.ui.FragmentScope


@Subcomponent(modules = [(ExpenseServiceModule::class)])
@FragmentScope
interface ExpenseSubcomponent : AndroidInjector<ExpenseFragment> {
    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<ExpenseFragment>()
}