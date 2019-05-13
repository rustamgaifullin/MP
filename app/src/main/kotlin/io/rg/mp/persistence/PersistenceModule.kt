package io.rg.mp.persistence

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class PersistenceModule {
    @Provides
    @Singleton
    fun database(context: Context): Database {
        return Room.databaseBuilder(context, Database::class.java, "mp-db").build()
    }

    @Provides
    @Singleton
    fun categoryDao(database: Database) = database.categoryDao()

    @Provides
    @Singleton
    fun spreadsheetDao(database: Database) = database.spreadsheetDao()

    @Provides
    @Singleton
    fun transactionDao(database: Database) = database.transactionDao()

    @Provides
    @Singleton
    fun failedSpreadsheetDao(database: Database) = database.failedSpreadsheetDao()
}