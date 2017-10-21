package io.rg.mp.service.sheet

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import io.reactivex.subscribers.TestSubscriber
import io.rg.mp.persistence.entity.Category
import io.rg.mp.service.data.Expense
import io.rg.mp.service.data.Result
import io.rg.mp.service.data.Success
import org.junit.Test
import org.mockito.Mockito
import java.util.*

class ExpenseServiceTest {

    private val credentialMock = Mockito.mock(GoogleAccountCredential::class.java)

    @Test
    fun should_successfully_save_expense_item() {
        //given
        val sut = ExpenseService(credentialMock)
        val testSubscriber = TestSubscriber<Result>()

        //when
        sut.save(Expense(Date(), 5.5f, "", Category("", ""))).subscribe(testSubscriber)

        //then
        testSubscriber.assertNoErrors()
        testSubscriber.assertValue { result ->  result is Success }
        testSubscriber.assertComplete()
    }
}