package io.rg.mp.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import dagger.android.AndroidInjection
import io.rg.mp.R
import io.rg.mp.utils.Preferences
import kotlinx.android.synthetic.main.activity_main.toolbar
import javax.inject.Inject


class MainActivity : AppCompatActivity() {

    @Inject lateinit var preferences: Preferences

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)
        navController = findNavController(R.id.nav_host)
        appBarConfiguration = AppBarConfiguration(setOf(R.id.spreadsheetScreen, R.id.authScreen))
        toolbar.setupWithNavController(navController, appBarConfiguration)

        if (!preferences.isAccountNameAvailable) {
            navController.navigate(R.id.actionShowLoginScreen)
        }
    }
}