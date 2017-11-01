package io.rg.mp.ui.expense

import android.support.v4.app.Fragment
import dagger.Binds
import dagger.Module
import dagger.android.AndroidInjector
import dagger.android.support.FragmentKey
import dagger.multibindings.IntoMap


@Module (subcomponents = arrayOf(ExpenseSubcomponent::class))
abstract class ExpenseFragmentModule {
    @Binds
    @IntoMap
    @FragmentKey(ExpenseFragment::class)
    abstract fun bindMainFragment(builder: ExpenseSubcomponent.Builder)
            : AndroidInjector.Factory<out Fragment>
}