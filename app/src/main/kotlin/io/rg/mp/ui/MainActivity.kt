package io.rg.mp.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import dagger.android.AndroidInjection
import io.rg.mp.R
import io.rg.mp.ui.auth.AuthFragment
import io.rg.mp.ui.expense.ExpenseFragment
import io.rg.mp.utils.Preferences
import javax.inject.Inject


class MainActivity : AppCompatActivity() {

    @Inject lateinit var preferences: Preferences

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            initFragments()
        }
    }

    private fun initFragments() {
        val transaction = supportFragmentManager.beginTransaction()

        if (preferences.isAccountNameAvailable) {
            transaction.add(R.id.main_container, ExpenseFragment())
        } else {
            transaction.add(R.id.main_container, AuthFragment())
        }

        transaction.commit()
    }
}