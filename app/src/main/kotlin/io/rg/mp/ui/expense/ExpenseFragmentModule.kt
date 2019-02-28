package io.rg.mp.ui.expense

import androidx.fragment.app.Fragment
import dagger.Binds
import dagger.Module
import dagger.android.AndroidInjector
import dagger.android.support.FragmentKey
import dagger.multibindings.IntoMap


@Module (subcomponents = [(ExpenseSubcomponent::class)])
abstract class ExpenseFragmentModule {
    @Binds
    @IntoMap
    @FragmentKey(ExpenseFragment::class)
    abstract fun bindExpenseFragment(builder: ExpenseSubcomponent.Builder)
            : AndroidInjector.Factory<out Fragment>
}