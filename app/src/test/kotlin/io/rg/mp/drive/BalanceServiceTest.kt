package io.rg.mp.drive

import com.google.api.client.testing.http.MockLowLevelHttpResponse
import io.rg.mp.balance
import io.rg.mp.drive.data.Balance
import io.rg.mp.emptyBalance
import io.rg.mp.mockResponse
import io.rg.mp.mockSheetClient
import io.rg.mp.partialBalance
import org.junit.Test
import java.util.LinkedList

class BalanceServiceTest : SubscribableTest<Balance>() {
    @Test
    fun `should return balance response`() {
        val responses = LinkedList<MockLowLevelHttpResponse>()
        responses.add(mockResponse(balance()))

        val sut = BalanceService(mockSheetClient(responses))

        sut.retrieve("any").toFlowable().subscribe(testSubscriber)

        testSubscriber.assertNoErrors()
                .assertValue {
                    it == Balance("2000", "200", "1000")
                }
    }

    @Test
    fun `should return empty balance`() {
        val responses = LinkedList<MockLowLevelHttpResponse>()
        responses.add(mockResponse(emptyBalance()))

        val sut = BalanceService(mockSheetClient(responses))

        sut.retrieve("any").toFlowable().subscribe(testSubscriber)

        testSubscriber.assertNoErrors()
                .assertValue {
                    it == Balance()
                }
    }

    @Test
    fun `should return partial balance`() {
        val responses = LinkedList<MockLowLevelHttpResponse>()
        responses.add(mockResponse(partialBalance()))

        val sut = BalanceService(mockSheetClient(responses))

        sut.retrieve("any").toFlowable().subscribe(testSubscriber)

        testSubscriber.assertNoErrors()
                .assertValue {
                    it == Balance(actual = "200")
                }
    }
}