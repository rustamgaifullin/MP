package io.rg.mp.app

import android.app.Activity
import androidx.multidex.MultiDexApplication
import androidx.fragment.app.Fragment
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.support.HasSupportFragmentInjector
import io.rg.mp.persistence.PersistenceModule
import io.rg.mp.drive.ServiceModule
import javax.inject.Inject

class MpApplication : MultiDexApplication(), HasActivityInjector, HasSupportFragmentInjector {
    @Inject lateinit var dispatchingFragmentInjector: DispatchingAndroidInjector<Fragment>
    @Inject lateinit var dispatchingActivityInjector: DispatchingAndroidInjector<Activity>

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

    override fun activityInjector() = dispatchingActivityInjector
}