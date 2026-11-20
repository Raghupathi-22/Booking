package com.hyderabad.booking.common.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hyderabad.booking.common.network.ApiResult

/** Loading / error / data holder used by every list or detail screen. */
sealed interface Load<out T> {
    data object Loading : Load<Nothing>
    data class Failed(val message: String) : Load<Nothing>
    data class Ready<T>(val value: T) : Load<T>
}

fun <T> ApiResult<T>.toLoad(): Load<T> = when (this) {
    is ApiResult.Success -> Load.Ready(data)
    is ApiResult.Error -> Load.Failed(message)
}

/** `viewModel(factory = vmFactory { MyViewModel(api) })` */
inline fun <reified VM : ViewModel> vmFactory(crossinline builder: () -> VM): ViewModelProvider.Factory =
    object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = builder() as T
    }
