package io.rg.mp.app

import android.content.Context
import dagger.Module
import dagger.Provides
import io.rg.mp.utils.Preferences
import io.rg.mp.utils.Toasts

@Module
class AppModule(val context: Context) {

    @Provides fun context() = context
    @Provides fun toasts() = Toasts()
    @Provides fun preferences(context: Context) = Preferences(context)
}