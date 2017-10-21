package io.rg.mp.app

import dagger.Component
import dagger.android.support.AndroidSupportInjectionModule
import io.rg.mp.ui.main.MainFragmentModule
import javax.inject.Singleton

@Component(modules = arrayOf(
        AppModule::class,
        ServiceModule::class,
        PersistenceModule::class,
        AndroidSupportInjectionModule::class,
        MainFragmentModule::class
))
@Singleton
interface AppComponent {
    fun inject(app: MpApp)
}