package io.rg.mp.ui.transactions

import android.support.v4.app.Fragment
import dagger.Binds
import dagger.Module
import dagger.android.AndroidInjector
import dagger.android.support.FragmentKey
import dagger.multibindings.IntoMap

@Module(subcomponents = [(TransactionsSubcomponent::class)])
abstract class TransactionsFragmentModule {
    @Binds
    @IntoMap
    @FragmentKey(TransactionsFragment::class)
    abstract fun bindTransactionFragment(builder: TransactionsSubcomponent.Builder)
            : AndroidInjector.Factory<out Fragment>
}