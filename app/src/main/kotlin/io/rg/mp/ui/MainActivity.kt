package io.rg.mp.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import dagger.android.AndroidInjection
import io.rg.mp.R
import io.rg.mp.ui.auth.AuthFragment
import io.rg.mp.ui.expense.ExpenseFragment
import io.rg.mp.ui.spreadsheet.SpreadsheetFragment
import io.rg.mp.ui.transactions.TransactionsFragment
import io.rg.mp.utils.Preferences
import javax.inject.Inject


class MainActivity : AppCompatActivity() {

    @Inject lateinit var preferences: Preferences

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initFragments()
    }

    private fun initFragments() {
        if (preferences.isAccountNameAvailable) {
            addFragment(SpreadsheetFragment.NAME) { SpreadsheetFragment() }
        } else {
            addFragment(AuthFragment.NAME) { AuthFragment() }
        }
    }

    private fun addFragment(tag: String, fragment: () -> Fragment) {
        if (supportFragmentManager.findFragmentByTag(tag) == null) {
            supportFragmentManager.beginTransaction()
                    .add(R.id.main_container, fragment.invoke(), tag)
                    .commit()
        }
    }

    override fun onBackPressed() {
        when {
            supportFragmentManager.findFragmentByTag(TransactionsFragment.NAME) != null -> supportFragmentManager.popBackStack(
                    TransactionsFragment.NAME, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            supportFragmentManager.findFragmentByTag(ExpenseFragment.NAME) != null -> supportFragmentManager.popBackStack(
                    ExpenseFragment.NAME, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            else -> super.onBackPressed()
        }
    }
}