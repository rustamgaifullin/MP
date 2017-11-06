package io.rg.mp.persistence

import android.arch.persistence.room.Room
import android.content.Context
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
}