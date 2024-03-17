package com.example.bletest.presentation.scan

import android.Manifest
import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bletest.R
import com.example.bletest.bt.BLEScanner
import com.example.bletest.bt.needBtConnectPermission
import com.example.bletest.datalayer.repository.DeviceModel
import com.example.bletest.presentation.ErrorView
import com.example.bletest.presentation.LocalBluetoothIconIDs
import com.example.bletest.presentation.LocalSizes
import com.example.bletest.presentation.deviceDetail.DeviceParamsTabItem
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScannedDeviceDetailScreen(
    viewModel: ScannedDeviceDetailViewModel,
    deviceMac: String
) {
    val needPermission by remember { mutableStateOf(needBtConnectPermission()) }
    if(needPermission) {
        val permissionState = rememberPermissionState(permission = Manifest.permission.BLUETOOTH_CONNECT)
        if (permissionState.status.isGranted) {
            ObserveConnectionStatus(
                viewModel = viewModel,
                deviceMac = deviceMac
            )
        } else {
            SideEffect {
                permissionState.launchPermissionRequest()
            }
        }
    } else {
        ObserveConnectionStatus(
            viewModel = viewModel,
            deviceMac = deviceMac
        )
    }
}

@Composable
fun ObserveConnectionStatus(
    viewModel: ScannedDeviceDetailViewModel,
    deviceMac: String
) {
    LifecycleStartEffect(Unit) {
        viewModel.connectToDevice(deviceMac)
        onStopOrDispose {
            viewModel.disconnect()
        }
    }

    val uiState by viewModel.deviceState.collectAsStateWithLifecycle()
    when(uiState) {
        is ScannedDeviceDetailViewModel.UIState.UIDeviceModel -> {
            MainLayout(model = uiState as ScannedDeviceDetailViewModel.UIState.UIDeviceModel)
        }
        else ->{}
    }
}

@Composable
fun MainLayout(
    model: ScannedDeviceDetailViewModel.UIState.UIDeviceModel
) {
    Column(
        modifier = Modifier
            .padding(LocalSizes.current.paddingSmall)
            .fillMaxSize()
            .animateContentSize()
    ) {
        Header(
            model,
            Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
        )
        ResultContainer(
            model,
            Modifier
                .weight(1f)
                .background(MaterialTheme.colorScheme.tertiaryContainer)
        )
    }
}

@Composable
fun Header(
    model: ScannedDeviceDetailViewModel.UIState.UIDeviceModel,
    modifier: Modifier
) {
    Column(
        modifier = modifier
            .padding(LocalSizes.current.paddingSmall)
            .fillMaxWidth()
            .animateContentSize()
    ) {

        DeviceParamsTabItem(
            titleId = R.string.device_param_mac,
            itemValue = model.name,
            modifier = modifier
        )

        DeviceParamsTabItem(
            titleId = R.string.device_connection_status,
            itemValue = model.status,
            modifier = modifier
        )
    }
}

@Composable
fun ResultContainer(
    model: ScannedDeviceDetailViewModel.UIState.UIDeviceModel,
    modifier: Modifier
) {
    Column(
        modifier = modifier.padding(LocalSizes.current.paddingSmall),
        verticalArrangement = Arrangement.spacedBy(LocalSizes.current.paddingSmall)
    ) {
        if (model.error != null) {
            ErrorView(model.error.message)
        } else {
            if(model.services.isNotEmpty()) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.LightGray),
                    text = "Services:",
                    style = MaterialTheme.typography.bodyLarge
                )
                model.services.forEach {
                    key(it) {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.secondaryContainer),
                            text = it,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}