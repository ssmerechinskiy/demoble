package com.example.bletest.presentation.deviceDetail

import android.content.Context
import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.raise.fold
import com.example.bletest.R
import com.example.bletest.datalayer.repository.DataRepository
import com.example.bletest.datalayer.repository.DeviceModel
import com.example.bletest.presentation.deviceList.DevicesViewModel
import com.example.bletest.presentation.deviceList.NotifType
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeviceDetailViewModel @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
    private val savedStateHandle: SavedStateHandle,
    private val dataRepository: DataRepository
): ViewModel() {

    private val VMTAG = DevicesViewModel::class.simpleName

    private val _deviceState = MutableStateFlow<UIState>(UIState.Device())
    val deviceState : StateFlow<UIState> = _deviceState.asStateFlow()

    private lateinit var macAddress: String

    fun loadDevice(macAddress: String) {
        Log.i("SSLDVM", "loaddevice...")
        this.macAddress = macAddress
        getDevice()
    }

    private fun getDevice() {
        viewModelScope.launch {
            _deviceState.update { UIState.Device(inProgress = true, notifType = null) }
            fold(
                block = {
                    dataRepository.getDeviceModel(macAddress)
                },
                recover = { error ->
                    _deviceState.update {
                        UIState.Error(R.string.on_cache_error)
                    }
                },
                transform = { device ->
                    delay(500)
                    _deviceState.update {
                        UIState.Device(device, inProgress = false, notifType = null)
                    }
                }
            )
        }
    }

    sealed class UIState {
        data class Device(
            val device: DeviceModel? = null,
            val inProgress: Boolean = false,
            val notifType: NotifType? = null
        ) : UIState()
        data class Error(@StringRes val message: Int) : UIState()
    }

}





