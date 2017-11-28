package io.rg.mp.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import dagger.android.AndroidInjection
import io.rg.mp.R
import io.rg.mp.ui.auth.AuthFragment
import io.rg.mp.ui.expense.ExpenseFragment
import io.rg.mp.utils.Preferences
import javax.inject.Inject


class MainActivity : AppCompatActivity() {

    companion object {
        const val EXPENSE_FRAGMENT = "EXPENSE_FRAGMENT"
        const val AUTH_FRAGMENT = "AUTH_FRAGMENT"
    }

    @Inject lateinit var preferences: Preferences

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initFragments()
    }

    private fun initFragments() {
        if (preferences.isAccountNameAvailable) {
            addFragment(EXPENSE_FRAGMENT, {
                ExpenseFragment()
            })
        } else {
            addFragment(AUTH_FRAGMENT, { AuthFragment() })
        }
    }

    private fun addFragment(tag: String, fragment: () -> Fragment) {
        if (supportFragmentManager.findFragmentByTag(tag) == null) {
            supportFragmentManager.beginTransaction()
                    .add(R.id.main_container, fragment.invoke(), tag)
                    .commit()
        }
    }
}