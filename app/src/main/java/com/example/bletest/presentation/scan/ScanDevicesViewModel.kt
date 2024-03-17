package com.example.bletest.presentation.scan

import android.Manifest
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import androidx.core.app.ActivityCompat
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.example.bletest.bt.BLEScanner
import com.example.bletest.bt.ConnectionManagerProvider
import com.example.bletest.datalayer.repository.DataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScanDevicesViewModel @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
    private val savedStateHandle: SavedStateHandle,
    private val dataRepository: DataRepository,
    private val bleScanner : BLEScanner,
    private val connectionProvider: ConnectionManagerProvider
): ViewModel() {

    private val TAG = "SSL_SCAN_VM"

    init {
        Log.i(TAG, "on init()")
    }

    override fun onCleared() {
        Log.i(TAG, "onCleared()")
    }

    private var scanJob : Job?  = null

    val devices = mutableStateMapOf<String, UIDeviceModel>()
    private val devicesScanResults = mutableMapOf<String, ScanResult>()

    fun scanDevices() {
        Log.i(TAG, "try start scan")
        if(scanJob?.isActive == true) {
            Log.i(TAG, "already scanning...")
            return
        }
        Log.i(TAG, "starting")
        viewModelScope.launch {
            bleScanner.startScan()?.collect { result ->
                when(result) {
                    is Either.Left -> {
                        Log.e(TAG, "error:${result.value}")
                    }
                    is Either.Right -> {
//                        Log.i(SSL_SCAN_VM, "result:${result.value}")
                        viewModelScope.launch {
                            result.value
                                .filter { it.scanRecord != null }
                                .forEach {
                                    if (ActivityCompat.checkSelfPermission(
                                            applicationContext,
                                            Manifest.permission.BLUETOOTH_CONNECT
                                        ) != PackageManager.PERMISSION_GRANTED) {
                                        Log.i(TAG, "scanResult name:${it.scanRecord?.deviceName}")
                                        Log.i(TAG, "scanResult device name:${it.device.name}")
                                        val model = UIDeviceModel(name = it.rssi.toString(), address = it.device.address)
                                        devices[it.device.address] = model
                                        devicesScanResults[it.device.address] = it
                                    }
                                }
                        }
                    }
                }
            }
        }.also { scanJob = it }
    }

    fun stopScanner() {
        bleScanner.stopScanner()
    }

    fun onDeviceClick(macAddress : String) {
        devicesScanResults[macAddress]?.let { scanRes ->
            connectionProvider.createConnectionManager(scanRes.device)
        }
        bleScanner.stopScanner()
    }

    data class UIDeviceModel(val name: String, val address: String)
}







