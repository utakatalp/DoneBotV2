package com.todoapp.uikit.extensions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@Composable
fun <T> Flow<T>.collectWithLifecycle(
    lifecycleOwner: LifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.(T) -> Unit,
) {
    LaunchedEffect(this, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(minActiveState) {
            if (context == EmptyCoroutineContext) {
                this@collectWithLifecycle.collect { block.invoke(this, it) }
            } else {
                withContext(context) {
                    this@collectWithLifecycle.collect { block.invoke(this, it) }
                }
            }
        }
    }
}

@Composable
fun <T> Flow<T>.CollectWithLaunchedEffect(result: suspend (T) -> Unit) {
    LaunchedEffect(this) {
        collect { effect ->
            result(effect)
        }
    }
}
