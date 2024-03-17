package com.example.bletest.presentation

import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.bletest.bt.BLEScanner
import com.example.bletest.presentation.deviceDetail.DeviceDetailScreen
import com.example.bletest.presentation.deviceDetail.DeviceDetailViewModel
import com.example.bletest.presentation.deviceList.DevicesScreen
import com.example.bletest.presentation.deviceList.DevicesViewModel
import com.example.bletest.presentation.scan.ObserveBluetoothDevices
import com.example.bletest.presentation.scan.ScanDevicesViewModel
import com.example.bletest.presentation.scan.ScannedDeviceDetailScreen
import com.example.bletest.presentation.scan.ScannedDeviceDetailViewModel
import com.example.bletest.presentation.scan.SearchDevicesScreen
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

enum class AppScreen { DeviceList, DeviceDetail, BtScanner, BtDeviceDetail }

const val PARAM_MAC_ADDRESS = "mac"
const val PARAM_IMAGE_INDEX = "image_index"

sealed class NavigationItem(val route: String, val baseRoute: String = "") {
    data object Devices : NavigationItem(AppScreen.DeviceList.name)
    data object DeviceDetail : NavigationItem(
        route = AppScreen.DeviceDetail.name + "/{${PARAM_MAC_ADDRESS}}/{${PARAM_IMAGE_INDEX}}",
        baseRoute = AppScreen.DeviceDetail.name
    )
    data object BtDeviceDetail : NavigationItem(
        route = AppScreen.BtDeviceDetail.name + "/{${PARAM_MAC_ADDRESS}}",
        baseRoute = AppScreen.BtDeviceDetail.name
    )
    data object Scanner : NavigationItem(AppScreen.BtScanner.name)
}

@Composable
fun AppNavigationHost(
    navController: NavHostController,
    startDestination: String = NavigationItem.Devices.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(NavigationItem.Devices.route) {
            DevicesScreen(
                viewModel = hiltViewModel<DevicesViewModel>(),
                onItemClick = {
                    navController.navigate(NavigationItem.DeviceDetail.baseRoute + "/${it.deviceModel.macAddress}/${it.imageIndex}")
                },
                onScanButtonClick = {
                    navController.navigate(NavigationItem.Scanner.route)
                }
            )
        }
        composable(
            NavigationItem.DeviceDetail.route,
            arguments = listOf(
                navArgument(PARAM_MAC_ADDRESS){ type = NavType.StringType },
                navArgument(PARAM_IMAGE_INDEX){ type = NavType.IntType }
            )
        ) {backStackEntry ->
            val mac = backStackEntry.arguments?.getString(PARAM_MAC_ADDRESS) ?: ""
            val imageIndex = backStackEntry.arguments?.getInt(PARAM_IMAGE_INDEX) ?: 0
            DeviceDetailScreen(
                viewModel = hiltViewModel<DeviceDetailViewModel>(),
                deviceMac = mac,
                imageIndex = imageIndex
            )
        }
        composable(NavigationItem.Scanner.route) {
            SearchDevicesScreen(
                viewModel = hiltViewModel<ScanDevicesViewModel>(),
                onItemClick = {
                    navController.navigate(NavigationItem.BtDeviceDetail.baseRoute + "/${it}")
                }
            )
        }
        composable(
            NavigationItem.BtDeviceDetail.route,
            arguments = listOf(
                navArgument(PARAM_MAC_ADDRESS){ type = NavType.StringType }
            )
        ) { backStackEntry ->
            val mac = backStackEntry.arguments?.getString(PARAM_MAC_ADDRESS) ?: ""
            ScannedDeviceDetailScreen(
                viewModel = hiltViewModel<ScannedDeviceDetailViewModel>(),
                deviceMac = mac
            )
        }
    }
}