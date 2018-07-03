package space.naboo.memory.extensions

import org.mockito.ArgumentCaptor
import org.mockito.Mockito

internal fun <T> any(): T {
    Mockito.any<T>()
    return uninitialized()
}
private fun <T> uninitialized(): T = null as T

fun <T> capture(argumentCaptor: ArgumentCaptor<T>): T = argumentCaptor.capture()
