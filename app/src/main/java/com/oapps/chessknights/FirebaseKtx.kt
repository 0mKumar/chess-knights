package com.oapps.chessknights

import androidx.compose.runtime.*
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Composable
fun FirebaseAuth.currentUserAsState(): State<FirebaseUser?> {
    val currentUser = remember { mutableStateOf(currentUser) }
    DisposableEffect(this) {
        val callback = FirebaseAuth.AuthStateListener { auth ->
            currentUser.value = auth.currentUser
        }
        addAuthStateListener(callback)
        onDispose {
            removeAuthStateListener(callback)
        }
    }
    return currentUser
}

@ExperimentalCoroutinesApi
suspend fun <T> Task<T>.await(): T {
    // fast path
    if (isComplete) {
        val e = exception
        return if (e == null) {
            if (isCanceled) {
                throw CancellationException("Task $this was cancelled normally.")
            } else {
                @Suppress("UNCHECKED_CAST")
                result as T
            }
        } else {
            throw e
        }
    }

    return suspendCancellableCoroutine { cont ->
        addOnCompleteListener {
            val e = exception
            if (e == null) {
                @Suppress("UNCHECKED_CAST")
                if (isCanceled) cont.cancel() else cont.resume(result as T){
                    cont.resumeWithException(it)
                }
            } else {
                cont.resumeWithException(e)
            }
        }
    }
}

suspend fun DatabaseReference.pushValue(value: Any) =  suspendCoroutine<String> { continuation ->
    push().setValue(value) { databaseError: DatabaseError?, databaseReference: DatabaseReference ->
        if (databaseError != null) {
            continuation.resumeWithException(databaseError.toException())
        } else {
            continuation.resume(databaseReference.key!!)
        }
    }
}