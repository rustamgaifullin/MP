package io.rg.mp.app

import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.support.AndroidSupportInjectionModule
import io.rg.mp.persistence.PersistenceModule
import io.rg.mp.drive.ServiceModule
import io.rg.mp.ui.MainActivityModule
import io.rg.mp.ui.auth.AuthFragmentModule
import io.rg.mp.ui.expense.ExpenseFragmentModule
import javax.inject.Singleton

@Component(modules = arrayOf(
        AppModule::class,
        ServiceModule::class,
        PersistenceModule::class,
        AndroidInjectionModule::class,
        AndroidSupportInjectionModule::class,
        ExpenseFragmentModule::class,
        AuthFragmentModule::class,
        MainActivityModule::class
))
@Singleton
interface AppComponent {
    fun inject(application: MpApplication)
}