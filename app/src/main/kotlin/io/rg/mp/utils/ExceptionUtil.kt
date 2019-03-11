package io.rg.mp.utils

import android.util.Log
import io.reactivex.FlowableEmitter
import io.reactivex.SingleEmitter
import java.io.InterruptedIOException

inline fun onErrorIfNotCancelled(emitter: FlowableEmitter<*>, action: () -> Unit) {
    try {
        action()
    } catch (e: InterruptedIOException) {
        Log.d("ExceptionUtil",
                "Interrupted exception was thrown. " +
                        "emitter.isCancelled = ${emitter.isCancelled}")
        if (!emitter.isCancelled) {
            emitter.onError(e)
        }
    }
}

inline fun onErrorIfNotDisposed(emitter: SingleEmitter<*>, action: () -> Unit) {
    try {
        action()
    } catch (e: InterruptedIOException) {
        Log.d("ExceptionUtil",
                "Interrupted exception was thrown. " +
                        "emitter.isDisposed = ${emitter.isDisposed}")
        if (!emitter.isDisposed) {
            emitter.onError(e)
        }
    }
}