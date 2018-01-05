package io.rg.mp.drive

import io.reactivex.subscribers.TestSubscriber
import org.junit.Before

abstract class SubscribableTest<T> {
    protected lateinit var testSubscriber: TestSubscriber<T>

    @Before
    fun configure() {
        testSubscriber = TestSubscriber()
    }
}