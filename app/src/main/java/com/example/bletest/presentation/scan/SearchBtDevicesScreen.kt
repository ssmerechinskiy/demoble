package com.example.bletest.presentation.scan

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Info
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import com.example.bletest.R
import com.example.bletest.bt.BLEScanner
import com.example.bletest.presentation.LocalSizes
import com.example.bletest.presentation.MyListItem
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SearchDevicesScreen(
    viewModel: ScanDevicesViewModel = hiltViewModel<ScanDevicesViewModel>(),
    onItemClick : (String) -> Unit
) {

    var permission by remember { mutableStateOf(BLEScanner.getScanPermission()) }
    val permissionState = rememberPermissionState(permission = permission)
    if (permissionState.status.isGranted) {
        ObserveBluetoothDevices(
            viewModel, onItemClick = {
                viewModel.onDeviceClick(it.address)
                onItemClick(it.address)
            }
        )
    } else {
        SideEffect {
            permissionState.launchPermissionRequest()
        }
    }
}

@Composable
fun ObserveBluetoothDevices(
    viewModel: ScanDevicesViewModel = hiltViewModel<ScanDevicesViewModel>(),
    onItemClick : (ScanDevicesViewModel.UIDeviceModel) -> Unit
) {
    LifecycleStartEffect {
        viewModel.scanDevices()
        onStopOrDispose {
            viewModel.stopScanner()
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
        ) {
            items(
                items = viewModel.devices.values.toList(),
                key = { device -> device.address }
            ) {device ->
                MyListItem(
                    title = device.address,
                    subTitle = device.name,
                    imageId = R.drawable.bluetooth_4
                ) { onItemClick(device) }
            }
        }
    }
}
