package io.rg.mp.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import dagger.android.AndroidInjection
import io.rg.mp.R
import io.rg.mp.utils.Preferences
import javax.inject.Inject


class MainActivity : AppCompatActivity() {

    @Inject lateinit var preferences: Preferences

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navController = findNavController(R.id.nav_host)
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
//        initFragments()
    }
//
//    private fun initFragments() {
//        if (preferences.isAccountNameAvailable) {
//            val finalHost = NavHostFragment.createArgs(R.navigation.nav_graph)
//            supportFragmentManager.beginTransaction()
//                    .replace(R.id.nav_host, finalHost)
//                    .setPrimaryNavigationFragment(finalHost) // this is the equivalent to app:defaultNavHost="true"
//                    .commit()
//        } else {
//            addFragment(AuthFragment.NAME) { AuthFragment() }
//        }
//    }
//
//    private fun addFragment(tag: String, fragment: () -> Fragment) {
//        if (supportFragmentManager.findFragmentByTag(tag) == null) {
//            supportFragmentManager.beginTransaction()
//                    .replace(R.id.nav_host, fragment.invoke(), tag)
//                    .commit()
//        }
//    }

//    override fun onBackPressed() {
//        when {
//            supportFragmentManager.findFragmentByTag(TransactionsFragment.NAME) != null -> supportFragmentManager.popBackStack(
//                    TransactionsFragment.NAME, FragmentManager.POP_BACK_STACK_INCLUSIVE)
//            supportFragmentManager.findFragmentByTag(ExpenseFragment.NAME) != null -> supportFragmentManager.popBackStack(
//                    ExpenseFragment.NAME, FragmentManager.POP_BACK_STACK_INCLUSIVE)
//            else -> super.onBackPressed()
//        }
//    }
}