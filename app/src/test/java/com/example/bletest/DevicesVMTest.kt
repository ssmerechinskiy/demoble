package com.example.bletest

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import app.cash.turbine.turbineScope
import arrow.core.raise.Raise
import com.example.bletest.datalayer.db.GeneralError
import com.example.bletest.datalayer.network.ApiError
import com.example.bletest.datalayer.repository.DataLayerError
import com.example.bletest.datalayer.repository.DataRepository
import com.example.bletest.datalayer.repository.DeviceModel
import com.example.bletest.presentation.deviceList.DevicesViewModel
import com.example.bletest.presentation.deviceList.NotifType
import io.mockk.coEvery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.mock

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class DevicesVMTest {
    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
    }
    @After
    fun reset() {
        Dispatchers.resetMain()
    }

    private val mockDataRepositoryError = object : DataRepository {
        context(Raise<DataLayerError>)
        override suspend fun getDeviceModels(fromCache: Boolean): List<DeviceModel>  {
            println("getDeviceModels:$fromCache")
            if(!fromCache) {
                raise(DataLayerError.NetworkError(ApiError(400, "")))
            } else {
                raise(DataLayerError.StorageError(GeneralError("error reading from db")))
            }
        }

        context(Raise<DataLayerError>)
        override suspend fun getDeviceModel(macAddress: String): DeviceModel? = null
    }

    @Test
    fun loadDevices_repositoryErrors() = runTest {
        turbineScope {
            val viewModel = DevicesViewModel(SavedStateHandle(), mockDataRepositoryError)
            viewModel.loadDevices()
            viewModel.devicesState
                .onEach { println("state:$it") }
                .test {
                    //initial state
                    val check1 = DevicesViewModel.UIState.DevicesState(
                        devices = emptyList(), inProgress = false, notifType = null
                    )

                    assertEquals(check1, awaitItem())
                    //start progress
                    val check2 = DevicesViewModel.UIState.DevicesState(
                        devices = emptyList(), inProgress = true, notifType = null
                    )

                    assertEquals(check2, awaitItem())
                    //first network error occurred. Just show ToastNotification & waiting data from DB
                    val check3 = DevicesViewModel.UIState.DevicesState(
                        devices = emptyList(), inProgress = false, notifType = NotifType.ToastMessage(R.string.on_network_error)
                    )
                    assertEquals(check3, awaitItem())

                    //start progress
                    val check4 = DevicesViewModel.UIState.DevicesState(
                        devices = emptyList(), inProgress = true, notifType = null
                    )
                    assertEquals(check4, awaitItem())

                    //show error in UI
                    val check5 = DevicesViewModel.UIState.Error(R.string.on_cache_error)
                    assertEquals(check5, awaitItem())
                }
        }
    }

    @Test
    fun loadDevices_RepositorySuccess() = runTest {
        turbineScope {
            val viewModel = DevicesViewModel(SavedStateHandle(), mockDataRepositoryError)
            viewModel.loadDevices()
            viewModel.devicesState
                .onEach { println("state:$it") }
                .test {
                    //initial state
                    val check1 = DevicesViewModel.UIState.DevicesState(
                        devices = emptyList(), inProgress = false, notifType = null
                    )

                    assertEquals(check1, awaitItem())
                    //start progress
                    val check2 = DevicesViewModel.UIState.DevicesState(
                        devices = emptyList(), inProgress = true, notifType = null
                    )

                    assertEquals(check2, awaitItem())
                    //first network error occurred. Just show ToastNotification & waiting data from DB
                    val check3 = DevicesViewModel.UIState.DevicesState(
                        devices = emptyList(), inProgress = false, notifType = NotifType.ToastMessage(R.string.on_network_error)
                    )
                    assertEquals(check3, awaitItem())

                    //start progress
                    val check4 = DevicesViewModel.UIState.DevicesState(
                        devices = emptyList(), inProgress = true, notifType = null
                    )
                    assertEquals(check4, awaitItem())

                    //show error in UI
                    val check5 = DevicesViewModel.UIState.Error(R.string.on_cache_error)
                    assertEquals(check5, awaitItem())
                }
        }
    }


    private val mockDataRepositorySuccess = object : DataRepository {
        context(Raise<DataLayerError>)
        override suspend fun getDeviceModels(fromCache: Boolean): List<DeviceModel> = dbModelsStub
        context(Raise<DataLayerError>)
        override suspend fun getDeviceModel(macAddress: String): DeviceModel? = null
    }

}