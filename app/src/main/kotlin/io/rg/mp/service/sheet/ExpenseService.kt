package io.rg.mp.service.sheet

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import io.reactivex.Flowable
import io.rg.mp.service.data.Expense
import io.rg.mp.service.data.Result
import io.rg.mp.service.data.Success


class ExpenseService(val credential: GoogleAccountCredential) {
    fun save(data: Expense): Flowable<Result> {
        return Flowable.fromCallable { Success() }
    }
}