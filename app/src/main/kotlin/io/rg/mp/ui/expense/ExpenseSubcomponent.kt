package io.rg.mp.ui.expense

import dagger.Subcomponent
import dagger.android.AndroidInjector


@Subcomponent(modules = arrayOf(ExpenseServiceModule::class))
interface ExpenseSubcomponent : AndroidInjector<ExpenseFragment> {
    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<ExpenseFragment>()
}