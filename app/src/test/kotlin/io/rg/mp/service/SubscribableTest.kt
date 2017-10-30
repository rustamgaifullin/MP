package io.rg.mp.service

import io.reactivex.subscribers.TestSubscriber
import org.junit.Before

open class SubscribableTest<T> {
    protected lateinit var testSubscriber: TestSubscriber<T>

    @Before
    fun configure() {
        testSubscriber = TestSubscriber()
    }
}