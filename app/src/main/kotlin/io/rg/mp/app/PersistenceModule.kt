package io.rg.mp.app

import android.arch.persistence.room.Room
import android.content.Context
import dagger.Module
import dagger.Provides
import io.rg.mp.persistence.Database

@Module
class PersistenceModule {
    @Provides fun database(context: Context): Database {
        return Room.databaseBuilder(context, Database::class.java, "mp-db").build()
    }
    @Provides fun categoryDao(database: Database) = database.categoryDao()
    @Provides fun spreadsheetDao(database: Database) = database.spreadsheetDao()
}