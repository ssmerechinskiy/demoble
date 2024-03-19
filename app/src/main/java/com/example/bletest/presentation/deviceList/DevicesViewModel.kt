package com.example.bletest.presentation.deviceList

import android.content.Context
import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.raise.fold
import com.example.bletest.R
import com.example.bletest.datalayer.repository.DataLayerError
import com.example.bletest.datalayer.repository.DataRepository
import com.example.bletest.datalayer.repository.DeviceModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class NotifType {
    data class ToastMessage(@StringRes val message: Int) : NotifType()
    data class View(@StringRes val message: Int) : NotifType()
}

@HiltViewModel
class DevicesViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val dataRepository: DataRepository
): ViewModel() {

    private val VMTAG = DevicesViewModel::class.simpleName

    private val _devicesState = MutableStateFlow<UIState>(UIState.DevicesState())
    val devicesState : StateFlow<UIState> = _devicesState.asStateFlow()

    private var imageCount = 0

    fun loadDevices() {
        getDevices(useCache = false, onNetworkError = {
            _devicesState.update {
                UIState.DevicesState(
                    inProgress = false,
                    notifType = NotifType.ToastMessage(R.string.on_network_error)
                )
            }
            getDevices(useCache = true, delayMs = 500L, onStorageError = {
                _devicesState.update {
                    UIState.Error(R.string.on_cache_error)
                }
            })
        })
    }

    private fun getDevices(
        useCache : Boolean,
        onError: ((DataLayerError) -> Unit)? = null,
        onNetworkError: ((DataLayerError.NetworkError) -> Unit)? = null,
        onStorageError: ((DataLayerError.StorageError) -> Unit)? = null,
        delayMs: Long = 0
    ) {
        viewModelScope.launch {
            delay(delayMs)
            _devicesState.update { UIState.DevicesState(inProgress = true, notifType = null) }
            fold(
                block = {
                    dataRepository.getDeviceModels(useCache)
                },
                recover = { error ->
                    onError?.invoke(error)
                    when(error) {
                        is DataLayerError.NetworkError -> {
                            onNetworkError?.invoke(error)
                        }
                        is DataLayerError.StorageError -> {
                            onStorageError?.invoke(error)
                        }
                    }
                },
                transform = { devices ->
                    delay(500)
                    _devicesState.update {
                        UIState.DevicesState(
                            devices.toUiModel(),
                            inProgress = false,
                            notifType = null
                        )
                    }
                }
            )
        }
    }

    private fun List<DeviceModel>.toUiModel() : List<UIDeviceModel> {
        return this.map {
            UIDeviceModel(it, (0..< imageCount).random())
        }
    }

    fun setImageCount(count : Int) {
        imageCount = count
    }

    sealed class UIState {
        data class DevicesState(
            val devices: List<UIDeviceModel> = emptyList(),
            val inProgress: Boolean = false,
            val notifType: NotifType? = null
        ) : UIState()
        data class Error(@StringRes val message: Int) : UIState()
    }
}

data class UIDeviceModel(val deviceModel: DeviceModel, val imageIndex: Int)





