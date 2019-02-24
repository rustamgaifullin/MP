package io.rg.mp.ui.spreadsheet

import android.support.v4.app.Fragment
import dagger.Binds
import dagger.Module
import dagger.android.AndroidInjector
import dagger.android.support.FragmentKey
import dagger.multibindings.IntoMap

@Module (subcomponents = [(SpreadsheetSubcomponent::class)])
abstract class SpreadsheetFragmentModule {
    @Binds
    @IntoMap
    @FragmentKey(SpreadsheetFragment::class)
    abstract fun bindSpreadsheetFragment(builder: SpreadsheetSubcomponent.Builder)
            : AndroidInjector.Factory<out Fragment>
}