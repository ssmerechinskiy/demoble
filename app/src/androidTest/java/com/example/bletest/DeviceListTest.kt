package com.example.bletest

import androidx.activity.compose.setContent
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onChild
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.bletest.datalayer.repository.DeviceModel
import com.example.bletest.presentation.LocalBluetoothIconIDs
import com.example.bletest.presentation.LocalSizes
import com.example.bletest.presentation.MainActivity
import com.example.bletest.presentation.Sizes
import com.example.bletest.presentation.createBtIconsList
import com.example.bletest.presentation.deviceList.DevicesList
import com.example.bletest.presentation.deviceList.DevicesScreen
import com.example.bletest.presentation.deviceList.DevicesViewModel
import com.example.bletest.presentation.deviceList.UIDeviceModel
import com.example.bletest.presentation.deviceList.contentDescriptionDeviceList
import com.example.bletest.presentation.deviceList.contentDescriptionScanBleDevices
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DeviceListTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun checkFloatingActionButton() {
        composeTestRule.activity.setContent {
            CompositionLocalProvider(
                LocalBluetoothIconIDs provides createBtIconsList(),
                LocalSizes provides Sizes()
            ) {
                DevicesScreen(
                    viewModel = hiltViewModel<DevicesViewModel>(),
                    onItemClick = {
                        println("onItemClick item:$it")
                    },
                    onScanButtonClick = {
                        println("onScanClick item")
                    }
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(contentDescriptionScanBleDevices).also {
            it.assertIsDisplayed()
            it.assertIsEnabled()
            it.performClick()
        }

    }

    @OptIn(ExperimentalMaterialApi::class)
    @Test
    fun checkDevicesList() {
        composeTestRule.activity.setContent {
            CompositionLocalProvider(
                LocalBluetoothIconIDs provides createBtIconsList(),
                LocalSizes provides Sizes()
            ) {
                val viewModel = hiltViewModel<DevicesViewModel>()
                val models = listOf(
                    UIDeviceModel(dbModelsStub[0], 0),
                    UIDeviceModel(dbModelsStub[1], 1)
                )

                val pullToRefreshState = rememberPullRefreshState(
                    refreshing = false,
                    onRefresh = { viewModel.loadDevices() }
                )

                DevicesList(
                    devices = models,
                    inProgress = false,
                    pullToRefreshState = pullToRefreshState,
                    notifType = null,
                    onItemClick = {  }
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(contentDescriptionDeviceList).also {
            it.assertIsDisplayed()
            it.assertIsEnabled()

            it
                .onChildren()
                .onFirst()
                .assert(hasContentDescription("model1"))
        }
    }

}