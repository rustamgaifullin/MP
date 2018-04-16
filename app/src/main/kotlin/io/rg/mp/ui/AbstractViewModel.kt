package io.rg.mp.ui

import android.widget.Toast.LENGTH_LONG
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import io.rg.mp.R

open class AbstractViewModel {
    protected val subject = PublishSubject.create<ViewModelResult>()

    protected val progressSubject = BehaviorSubject.createDefault(0)

    fun viewModelNotifier(): Flowable<ViewModelResult> = subject.toFlowable(BackpressureStrategy.BUFFER)

    protected fun handleErrors(error: Throwable, requestCode: Int) {
        val result = when (error) {
            is UserRecoverableAuthIOException ->
                StartActivity(error.intent, requestCode)
            else -> {
                ToastInfo(R.string.unknown_error, LENGTH_LONG)
            }
        }

        subject.onNext(result)
    }

    fun isOperationInProgress(): Observable<Boolean> = progressSubject
            .scan({ sum, item -> sum + item })
            .map({ sum -> sum > 0 })
}