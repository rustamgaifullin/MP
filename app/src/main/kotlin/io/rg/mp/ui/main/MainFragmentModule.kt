package io.rg.mp.ui.main

import android.support.v4.app.Fragment
import dagger.Binds
import dagger.Module
import dagger.android.AndroidInjector
import dagger.android.support.FragmentKey
import dagger.multibindings.IntoMap


@Module (subcomponents = arrayOf(MainSubcomponent::class))
abstract class MainFragmentModule {
    @Binds
    @IntoMap
    @FragmentKey(MainFragment::class)
    abstract fun bindMainFragment(builder: MainSubcomponent.Builder)
            : AndroidInjector.Factory<out Fragment>
}