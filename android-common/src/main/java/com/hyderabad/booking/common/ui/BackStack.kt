package com.hyderabad.booking.common.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import java.io.Serializable

/**
 * A very small navigation stack (no extra navigation library needed).
 * Screens are Serializable data classes, so the stack survives rotation and process death.
 */
class BackStack<T : Serializable>(private val state: MutableState<List<T>>) {
    val current: T get() = state.value.last()
    val canPop: Boolean get() = state.value.size > 1

    fun push(screen: T) { state.value = state.value + screen }
    fun pop() { if (canPop) state.value = state.value.dropLast(1) }
    fun replace(screen: T) { state.value = state.value.dropLast(1) + screen }
    fun reset(screen: T) { state.value = listOf(screen) }
}

@Composable
fun <T : Serializable> rememberBackStack(start: T): BackStack<T> {
    val state = rememberSaveable { mutableStateOf<List<T>>(listOf(start)) }
    val stack = remember(state) { BackStack(state) }
    BackHandler(enabled = stack.canPop) { stack.pop() }
    return stack
}
