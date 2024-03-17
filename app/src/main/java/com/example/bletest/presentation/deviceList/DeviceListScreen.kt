package com.example.bletest.presentation.deviceList

import android.widget.Toast
import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.PullRefreshState
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bletest.R
import com.example.bletest.presentation.ErrorView
import com.example.bletest.presentation.LocalBluetoothIconIDs
import com.example.bletest.presentation.MyListItem
import kotlinx.coroutines.delay

@Composable
fun ScanAction(onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(48.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.End,
    ) {
        FloatingActionButton(
            modifier = Modifier.semantics {
                contentDescription = contentDescriptionScanBleDevices
            },
            onClick = { onClick() },
        ) {
            Icon(Icons.Filled.Search, "")
        }
    }

}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DevicesScreen(
    viewModel: DevicesViewModel,
    onItemClick : (UIDeviceModel) -> Unit,
    onScanButtonClick : ()-> Unit
) {
    viewModel.setImageCount(LocalBluetoothIconIDs.current.size)

    val uiState by viewModel.devicesState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadDevices()
    }

    val inProgress by remember { derivedStateOf {
        (uiState is DevicesViewModel.UIState.DevicesState) && ((uiState as DevicesViewModel.UIState.DevicesState).inProgress)
    } }

    val pullToRefreshState = rememberPullRefreshState(
        refreshing = inProgress,
        onRefresh = { viewModel.loadDevices() }
    )

    when(uiState) {
        is DevicesViewModel.UIState.DevicesState ->
            DevicesList(
                devices = (uiState as DevicesViewModel.UIState.DevicesState).devices,
                inProgress = inProgress,
                pullToRefreshState = pullToRefreshState,
                notifType = (uiState as DevicesViewModel.UIState.DevicesState).notifType,
                onItemClick = onItemClick
            )
        is DevicesViewModel.UIState.Error -> {
            ErrorView()
        }
    }

    ScanAction {
        onScanButtonClick()
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DevicesList(
    devices : List<UIDeviceModel>,
    inProgress : Boolean,
    pullToRefreshState: PullRefreshState,
    notifType: NotifType?,
    onItemClick : (UIDeviceModel) -> Unit
) {

    notifType?.let {
        when(it) {
            is NotifType.ToastMessage -> {
                val context = LocalContext.current
                Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
            }
            is NotifType.View -> {}
        }
    }

    if(devices.isNotEmpty()) {
        Box(Modifier.pullRefresh(pullToRefreshState)) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .semantics {
                        contentDescription = contentDescriptionScanBleDevices
                    }
            ) {
                items(devices, key = { device -> device.deviceModel.macAddress }) {device ->
                    MyListItem(
                        title = device.deviceModel.model,
                        subTitle = device.deviceModel.product,
                        imageId = LocalBluetoothIconIDs.current[device.imageIndex]
                    ) { onItemClick(device) }
                }
            }
            PullRefreshIndicator(inProgress, pullToRefreshState, Modifier.align(Alignment.TopCenter))
        }
    } else {
        EmptyListAnimation()
    }
}

@OptIn(ExperimentalAnimationGraphicsApi::class)
@Composable
fun EmptyListAnimation() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {

        val image = AnimatedImageVector.animatedVectorResource(R.drawable.ic_hourglass_animated)
        var atEnd by remember { mutableStateOf(false) }

        Image(
            painter = rememberAnimatedVectorPainter(image, atEnd),
            contentDescription = "EmptyListStub",
            modifier = Modifier.size(100.dp),
            contentScale = ContentScale.Crop
        )
        LaunchedEffect(Unit) {
            while (true) {
                delay(200)
                atEnd = !atEnd
            }
        }
    }
}

const val contentDescriptionScanBleDevices = "search ble devices"
const val contentDescriptionDeviceList = "models list"