package io.rg.mp.app

import android.content.Context
import dagger.Module
import dagger.Provides
import io.rg.mp.utils.Preferences
import io.rg.mp.utils.Toasts
import javax.inject.Singleton

@Module
class AppModule(val context: Context) {

    @Provides
    @Singleton
    fun context() = context

    @Provides
    @Singleton
    fun toasts() = Toasts()

    @Provides
    @Singleton
    fun preferences(context: Context) = Preferences(context)
}