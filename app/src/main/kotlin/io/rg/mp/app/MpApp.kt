package io.rg.mp.app

import android.app.Application
import android.support.v4.app.Fragment
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject

class MpApp : Application(), HasSupportFragmentInjector {
    @Inject lateinit var dispatchingFragmentInjector: DispatchingAndroidInjector<Fragment>

    override fun onCreate() {
        super.onCreate()

        DaggerAppComponent
                .builder()
                .appModule(AppModule(applicationContext))
                .serviceModule(ServiceModule())
                .persistenceModule(PersistenceModule())
                .build()
                .inject(this)
    }

    override fun supportFragmentInjector() = dispatchingFragmentInjector
}