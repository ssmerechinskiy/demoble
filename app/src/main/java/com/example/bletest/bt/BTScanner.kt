package com.example.bletest.bt

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject


class BLEScanner @Inject constructor(
    @ApplicationContext private val appContext : Context
) {

    private val TAG = "SSL BLEScanner"

//    @Volatile private var state : StateFlow<Either<ScanError, List<ScanResult>>>? = null

    @Volatile private var scanning = false

    @Volatile private var producerScope: ProducerScope<Either<ScanError, List<ScanResult>>> ? = null

//    suspend fun startScan(coroutineScope: CoroutineScope) : StateFlow<Either<ScanError, List<ScanResult>>> {
//        Log.i(TAG, "try start scanning:$scanning state:$state")
//        if (scanning && state == null) throw IllegalStateException("scanning in process but state does not exist!!!")
//        if (!scanning) {
//            Log.i(TAG, "almost ready")
//            state = startScan().stateIn(coroutineScope)
//        }
//        return state!!
//    }

    suspend fun startScan() : Flow<Either<ScanError, List<ScanResult>>>? {
        return if (!scanning) startScanInternal()
        else null
    }

    private fun startScanInternal() : Flow<Either<ScanError, List<ScanResult>>> = callbackFlow {
        producerScope = this

        if(!appContext.bluetoothAdapter().isEnabled) {
            trySend(ScanError.BluetoothNotEnabled.left())
            close()
            return@callbackFlow
        }

        val scanner = appContext.bluetoothAdapter().bluetoothLeScanner
        if(scanner == null) {
            trySend(ScanError.ScannerNotExists.left())
            close()
            return@callbackFlow
        }

        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
            .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
            .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT)
            .setReportDelay(0L)
            .build()

        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                super.onScanResult(callbackType, result)
                Log.i(TAG, "onScanResult:$result")
                val res = result?.let {
                    listOf(it)
                } ?: emptyList()
                if (res.isNotEmpty()) trySend(res.right())
            }

            override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                super.onBatchScanResults(results)
                Log.i(TAG, "onBatchScanResult:${results?.size ?: -1}")
                val res = results?.toList() ?: emptyList()
                if (res.isNotEmpty()) trySend(res.right())
            }

            override fun onScanFailed(errorCode: Int) {
                super.onScanFailed(errorCode)
                Log.e(TAG, "onScanFailed:$errorCode")
                trySend(ScanError.GenScanError(errorCode).left())
                close()
            }
        }

        val permissionToCheck = getScanPermission()
        if (ActivityCompat.checkSelfPermission(appContext, permissionToCheck) != PackageManager.PERMISSION_GRANTED) {
            trySend(ScanError.PermissionNotGranted(permissionToCheck).left())
            close()
            return@callbackFlow
        }

        try {
            scanner.startScan(
                listOf<ScanFilter>(),
                scanSettings,
                scanCallback
            )
        } catch (t: Throwable) {
            Log.e(TAG, "start scan error:$t")
        }


        scanning = true

        awaitClose {
            Log.i(TAG, "onClose:Close resources")
            scanner?.stopScan(scanCallback)
            scanning = false
            producerScope = null
        }
    }

    fun stopScanner() {
        producerScope?.close()
        producerScope = null
    }

    sealed class ScanError {
        data class PermissionNotGranted(val permission: String) : ScanError()
        data object ScannerNotExists : ScanError()
        data object BluetoothNotEnabled : ScanError()
        data class GenScanError(val code: Int) : ScanError()
    }

    companion object {
        fun getScanPermission() : String {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Manifest.permission.BLUETOOTH_SCAN
            } else {
                Manifest.permission.ACCESS_COARSE_LOCATION
            }
        }
    }
}

fun Context.bluetoothAdapter() : BluetoothAdapter =
    (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter

