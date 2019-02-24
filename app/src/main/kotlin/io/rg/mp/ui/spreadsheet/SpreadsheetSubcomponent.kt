package io.rg.mp.ui.spreadsheet

import dagger.Subcomponent
import dagger.android.AndroidInjector
import io.rg.mp.ui.FragmentScope

@Subcomponent(modules = [(SpreadsheetServiceModule::class)])
@FragmentScope
interface SpreadsheetSubcomponent : AndroidInjector<SpreadsheetFragment> {
    @Subcomponent.Builder
    abstract class Builder: AndroidInjector.Builder<SpreadsheetFragment>()
}