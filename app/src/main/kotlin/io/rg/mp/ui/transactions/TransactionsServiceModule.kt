package io.rg.mp.ui.transactions

import com.google.api.services.sheets.v4.Sheets
import dagger.Module
import dagger.Provides
import io.rg.mp.drive.TransactionService
import io.rg.mp.persistence.dao.TransactionDao
import io.rg.mp.ui.FragmentScope

@Module
class TransactionsServiceModule {
    @Provides
    @FragmentScope
    fun transactionService(sheets: Sheets) = TransactionService(sheets)

    @Provides
    @FragmentScope
    fun transactionsViewModel(
            transactionDao: TransactionDao,
            transactionService: TransactionService): TransactionsViewModel {
        return TransactionsViewModel(transactionDao, transactionService)
    }
}