package io.rg.mp.ui.auth

import androidx.fragment.app.Fragment
import dagger.Binds
import dagger.Module
import dagger.android.AndroidInjector
import dagger.android.support.FragmentKey
import dagger.multibindings.IntoMap


@Module (subcomponents = arrayOf(AuthSubcomponent::class))
abstract class AuthFragmentModule {
    @Binds
    @IntoMap
    @FragmentKey(AuthFragment::class)
    abstract fun bindAuthFragment(builder: AuthSubcomponent.Builder)
            : AndroidInjector.Factory<out Fragment>
}