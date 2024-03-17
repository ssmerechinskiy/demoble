package com.example.bletest.bt

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice.BOND_BONDED
import android.bluetooth.BluetoothDevice.BOND_NONE
import android.bluetooth.BluetoothDevice.TRANSPORT_LE
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGatt.GATT_SUCCESS
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.example.bletest.di.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.launch
import javax.inject.Inject


class ConnectionManagerProvider @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
    @IoDispatcher private val dispatcher : CoroutineDispatcher,
    private val appScope : CoroutineScope,
) {
    private val connectors = HashMap<String, ConnectionManager>()

    fun createConnectionManager(device: BluetoothDevice) : ConnectionManager {
        val manager = ConnectionManager(device, applicationContext, appScope, dispatcher)
        connectors[device.address] = manager
        return manager
    }

    fun getConnectionManager(address : String) : ConnectionManager? = connectors[address]

    fun reset() {
        appScope.launch(dispatcher) {
            connectors.values.forEach {
                it.disconnect()
            }
            connectors.clear()
        }
    }
}

class ConnectionManager (
    private val device: BluetoothDevice,
    private val applicationContext: Context,
    private val appScope : CoroutineScope,
    private val dispatcher : CoroutineDispatcher
    ) {
    private val TAG ="SSL ConnectionManager"

    private var gatt : BluetoothGatt? = null

    private val _connectionState = MutableSharedFlow<ConnectionState>(
        replay = 1,
        extraBufferCapacity = 100
    )
    private val connectionState : SharedFlow<ConnectionState> = _connectionState.asSharedFlow()


    val services = HashMap<String, BluetoothGattService>()

    private val callback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            Log.i(TAG, "onConnectionStateChange: status:${status} newState:${newState}")
            appScope.launch {
                handleOnConnectionChange(gatt, status, newState)
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)
            Log.i(TAG, "onServiceDiscovered: status:${status}")
            gatt?.services?.forEach {
                services[it.uuid.toString()] = it
                Log.i(TAG, "onServiceDiscovered: service:${it.uuid}")
            }
            updateStateFlow(ConnectionState.Ready(services = services.keys.toList()))
        }

        override fun onServiceChanged(gatt: BluetoothGatt) {
            super.onServiceChanged(gatt)
            Log.i(TAG, "onServiceChanged")
        }
    }

    fun connect() : SharedFlow<ConnectionState> {
        _connectionState.resetReplayCache()
        val permissionCheckPassed =
            !(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                    && ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED)
        if (permissionCheckPassed) {
            Log.i(TAG, "permission check passed.connect Gatt")
            updateStateFlow(ConnectionState.Connecting)
            gatt = device.connectGatt(applicationContext, false, callback, TRANSPORT_LE)
        } else {
            updateStateFlow(
                ConnectionState.Disconnected(ConnectionError.PermissionNotGranted(Manifest.permission.BLUETOOTH_CONNECT))
            )
        }

        return connectionState
    }


    suspend fun handleOnConnectionChange(gatt: BluetoothGatt, status: Int, newState: Int) {
        Log.i(TAG, "handleOnConnectionChange")
        if(!applicationContext.bluetoothAdapter().isEnabled) {
            updateStateFlow(
                ConnectionState.Disconnected(ConnectionError.BluetoothNotEnabled)
            )
        }

        val permissionCheckPassed =
            !(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                    && ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED)
        if(permissionCheckPassed) {
            Log.i(TAG, "permission check passed. Check statuses")
            if(status == GATT_SUCCESS) {
                Log.i(TAG, "on GATT_SUCCESS:$status")
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.i(TAG, "new state BluetoothProfile.STATE_CONNECTED:$newState")
                    updateStateFlow(ConnectionState.Connected())
                    //check bond staff
                    val bondState = device.getBondState()
                    if(bondState == BOND_NONE || bondState == BOND_BONDED) {
                        delay(1000)
                        updateStateFlow(ConnectionState.Connected(SetupStatus.SERVICES_DISCOVERING))
                        gatt.discoverServices()
                    } else {
                        Log.i(TAG, "waiting for a bond")
                        updateStateFlow(ConnectionState.Connected(SetupStatus.BOND_SETUP))
                    }
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    //normal disconnecting
                    gatt.close()
                    updateStateFlow(ConnectionState.Disconnected())
                } else {
                    // connecting or disconnecting
                }
            } else {
                gatt.close()
                updateStateFlow(
                    ConnectionState.Disconnected(ConnectionError.GenError(status))
                )
            }
        } else {
            updateStateFlow(
                ConnectionState.Disconnected(ConnectionError.PermissionNotGranted(Manifest.permission.BLUETOOTH_CONNECT))
            )
        }
    }

    suspend fun disconnect() {
        val permissionCheckPassed =
            !(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                    && ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED)
        if(permissionCheckPassed) {
            gatt?.let { bg ->
                updateStateFlow(ConnectionState.Disconnecting)
                bg.disconnect()
                Log.i(TAG, "disconnect request sent.waiting for disconnect event")
                _connectionState
                    .completeWithConnectionStatus(ConnectionStatus.DISCONNECTED)
                    .collect {
                        Log.i(TAG, "disconnected event received. close gatt")
                        bg.close()
                    }
            }
        } else {
            updateStateFlow(
                ConnectionState.Disconnected(ConnectionError.PermissionNotGranted(Manifest.permission.BLUETOOTH_CONNECT))
            )
        }
    }

    private fun updateStateFlow(state : ConnectionState) {
        appScope.launch {
            delay(100)
            state
                .also {
                    _connectionState.emit(it)
                }
        }
    }

    enum class ConnectionStatus {
        DISCONNECTED, DISCONNECTING, CONNECTING, CONNECTED, READY
    }

    enum class SetupStatus {
        DEFAULT, BOND_SETUP, SERVICES_DISCOVERING
    }

    sealed class ConnectionState(val status : ConnectionStatus) {
        data class Disconnected(val error: ConnectionError? = null) : ConnectionState(ConnectionStatus.DISCONNECTED)
        data object Connecting : ConnectionState(ConnectionStatus.CONNECTING)
        data object Disconnecting : ConnectionState(ConnectionStatus.DISCONNECTING)
        data class Connected(val setupStatus: SetupStatus = SetupStatus.DEFAULT) : ConnectionState(ConnectionStatus.CONNECTED)
        data class Ready(val services: List<String>) : ConnectionState(ConnectionStatus.READY)
    }

    sealed class ConnectionError {
        data class PermissionNotGranted(val permission: String) : ConnectionError()
        data object BluetoothNotEnabled : ConnectionError()
        data class GenError(val code: Int) : ConnectionError()
    }
}

fun Flow<ConnectionManager.ConnectionState>.completeWithConnectionStatus(
    status: ConnectionManager.ConnectionStatus
) = transformWhile {
    if(it.status == status) {
        emit(it)
        false
    } else true
}


fun needBtConnectPermission() : Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S