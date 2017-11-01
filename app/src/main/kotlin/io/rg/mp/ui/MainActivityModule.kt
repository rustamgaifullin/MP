package io.rg.mp.ui

import android.app.Activity
import dagger.Binds
import dagger.Module
import dagger.android.ActivityKey
import dagger.android.AndroidInjector
import dagger.multibindings.IntoMap


@Module (subcomponents = arrayOf(MainSubcomponent::class))
abstract class MainActivityModule {
    @Binds
    @IntoMap
    @ActivityKey(MainActivity::class)
    abstract fun bindMainActivity(builder: MainSubcomponent.Builder)
            : AndroidInjector.Factory<out Activity>
}