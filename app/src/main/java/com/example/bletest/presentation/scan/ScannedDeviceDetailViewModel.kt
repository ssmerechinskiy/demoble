package com.example.bletest.presentation.scan

import android.content.Context
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.example.bletest.bt.BLEScanner
import com.example.bletest.bt.ConnectionManager
import com.example.bletest.bt.ConnectionManagerProvider
import com.example.bletest.datalayer.repository.DataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScannedDeviceDetailViewModel @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
    private val savedStateHandle: SavedStateHandle,
    private val dataRepository: DataRepository,
    private val bleScanner : BLEScanner,
    private val connectionProvider: ConnectionManagerProvider
): ViewModel() {

    private val TAG = "SSL_CONNECT_VM"

    private var macAddress : String? = null

    private var connectionJob : Job?  = null

    private val _devicesState = MutableStateFlow<UIState>(UIState.UIDeviceModel("", ""))
    val deviceState : StateFlow<UIState> = _devicesState.asStateFlow()

    sealed class UIState {
        data class UIDeviceModel(
            val name: String,
            val status : String,
            val services: List<String> = emptyList(),
            val error : Error? = null
        ) : UIState()
        data class Error(val message : String) : UIState()
    }


    fun connectToDevice(macAddress : String) {
        this.macAddress = macAddress
        viewModelScope.launch {
            val connectionManager = connectionProvider.getConnectionManager(macAddress)
            connectionManager?.let { cm ->
                cm.connect()
                    .buffer(100)
                    .collect { state ->
                        Log.i(TAG, "onEvent:$state")
                        val statusStr : String
                        val services = mutableListOf<String>()
                        var error : UIState.Error? = null
                        when(state) {
                            is ConnectionManager.ConnectionState.Connected -> {
                                statusStr = state.status.name + ":" + state.setupStatus
                            }
                            ConnectionManager.ConnectionState.Connecting -> statusStr = state.status.name
                            is ConnectionManager.ConnectionState.Disconnected -> {
                                Log.i(TAG, "onDisconnected:$state")
                                Log.i(TAG, "onDisconnected error:${state.error}")
                                statusStr = state.status.name
                                state.error?.let {
                                    error = UIState.Error(it.toString())
                                }
                            }
                            ConnectionManager.ConnectionState.Disconnecting -> statusStr = state.status.name
                            is ConnectionManager.ConnectionState.Ready -> {
                                statusStr = state.status.name
                                services.addAll(state.services)
                            }
                        }
                        _devicesState.update {
                            UIState.UIDeviceModel(
                                name = macAddress,
                                status = statusStr,
                                services = services,
                                error = error
                            )
                        }
                }
            }
        }.also { connectionJob = it }
    }

    fun disconnect() {
        macAddress?.let {
            viewModelScope.launch {
                val connectionManager = connectionProvider.getConnectionManager(it)
                connectionManager?.disconnect()
            }
        }
    }
}







