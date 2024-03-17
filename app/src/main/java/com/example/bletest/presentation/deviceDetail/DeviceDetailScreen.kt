package com.example.bletest.presentation.deviceDetail

import androidx.annotation.StringRes
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bletest.R
import com.example.bletest.datalayer.repository.DeviceModel
import com.example.bletest.presentation.LocalBluetoothIconIDs
import com.example.bletest.presentation.LocalSizes


@Composable
fun DeviceDetailScreen(
    viewModel: DeviceDetailViewModel,
    deviceMac: String,
    imageIndex: Int
) {
    LaunchedEffect(Unit) {
        viewModel.loadDevice(deviceMac)
    }
    val uiState by viewModel.deviceState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .animateContentSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painterResource(LocalBluetoothIconIDs.current[imageIndex]),
            contentDescription = "DeviceImage",
            modifier = Modifier
                .padding(8.dp)
                .weight(1f)
        )
        DeviceParamView(
            state = uiState,
            modifier = Modifier.weight(2f)
        )
    }
}

@Composable
fun DeviceParamView(
    state : DeviceDetailViewModel.UIState,
    modifier: Modifier
) {
    when(state) {
        is DeviceDetailViewModel.UIState.Device -> {
            state.device?.let {
                DeviceParamsTab(model = it, modifier = modifier)
            }
        }
        is DeviceDetailViewModel.UIState.Error -> {}
    }
}

@Composable
fun DeviceParamsTab(
    model : DeviceModel,
    modifier: Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        DeviceParamsTabItem(
            titleId = R.string.device_param_product,
            itemValue = model.product,
            modifier = modifier
        )
        DeviceParamsTabItem(
            titleId = R.string.device_param_model,
            itemValue = model.model,
            modifier = modifier
        )
        DeviceParamsTabItem(
            titleId = R.string.device_param_serial,
            itemValue = model.serial,
            modifier = modifier
        )
        DeviceParamsTabItem(
            titleId = R.string.device_param_mac,
            itemValue = model.macAddress,
            modifier = modifier
        )
        DeviceParamsTabItem(
            titleId = R.string.device_param_brake_light,
            itemValue = model.brakeLight.toString(),
            modifier = modifier
        )
        DeviceParamsTabItem(
            titleId = R.string.device_param_light_mode,
            itemValue = model.lightMode.toString(),
            modifier = modifier
        )
        DeviceParamsTabItem(
            titleId = R.string.device_param_light_auto,
            itemValue = model.lightAuto.toString(),
            modifier = modifier
        )
        DeviceParamsTabItem(
            titleId = R.string.device_param_light_value,
            itemValue = model.lightValue.toString(),
            modifier = modifier
        )
        DeviceParamsTabItem(
            titleId = R.string.device_param_installation_mode,
            itemValue = model.installationMode.toString(),
            modifier = modifier
        )
        DeviceParamsTabItem(
            titleId = R.string.device_param_firmware,
            itemValue = model.firmwareVersion,
            modifier = modifier
        )

    }
}


@Composable
fun DeviceParamsTabItem(
    @StringRes titleId : Int,
    itemValue : String,
    modifier: Modifier
) {
    val resources = LocalContext.current.resources
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(LocalSizes.current.paddingXTiny)
        ) {
            Text(
                modifier = Modifier
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(LocalSizes.current.paddingXTiny),
                text = resources.getString(titleId),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                modifier = Modifier
                    .weight(2f)
                    .background(MaterialTheme.colorScheme.tertiaryContainer)
                    .padding(LocalSizes.current.paddingXTiny),
                text = itemValue,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}