package com.example.bletest

import android.content.Context
import arrow.core.Either
import arrow.core.raise.Raise
import arrow.core.raise.either
import arrow.core.raise.fold
import com.example.bletest.datalayer.network.ApiError
import com.example.bletest.datalayer.network.BaseRetrofitDataSource
import com.example.bletest.datalayer.network.DeviceAPI
import com.example.bletest.datalayer.network.DeviceResponseItem
import com.example.bletest.datalayer.network.DevicesResponse
import com.example.bletest.datalayer.network.GeneralError
import com.example.bletest.datalayer.network.NetworkDSError
import com.example.bletest.datalayer.network.NetworkDataSource
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import retrofit2.HttpException
import retrofit2.Response

@RunWith(MockitoJUnitRunner::class)
class NetworkDataSourceTest {

    @Mock
    private lateinit var deviceAPIOnSuccess: DeviceAPI
    @Mock
    private lateinit var deviceAPIOnError: DeviceAPI

    @Test
    fun checkSuccess() = runTest {
        deviceAPIOnSuccess = mock {
            onBlocking { it.getDevices() } doReturn  Response.success(200, apiResponseStub)
        }
        val networkDS = NetworkDataSourceStub(deviceAPIOnSuccess)
        val result = fold(
            block = { networkDS.getDevices() },
            recover = { listOf() },
            { it.devices }
        )
        assertEquals(result.size, apiResponseStub.devices.size)
    }

    @Test
    fun checkErrors() {
        check_ApiError()
        check_ApiError()
    }

    @Test
    private fun check_ApiError() = runTest {
        deviceAPIOnError = mock {
            onBlocking { it.getDevices() } doReturn Response.error(400, "my error message".toResponseBody())
        }
        val result = either {
            NetworkDataSourceStub(deviceAPIOnError).getDevices()
        }
        when(result) {
            is Either.Left -> assert(result.value is ApiError)
            is Either.Right -> assert(false)
        }
    }

    @Test
    private fun check_GeneralError() = runTest {
        deviceAPIOnError = mock {
            onBlocking { it.getDevices() } doReturn Response.error(404, "my error message".toResponseBody())
        }
        val result = either {
            NetworkDataSourceStub(deviceAPIOnError).getDevicesWithException()
        }
        when(result) {
            is Either.Left -> assert(result.value is GeneralError)
            is Either.Right -> assert(false)
        }
    }
}

class NetworkDataSourceStub(
    private val deviceApi: DeviceAPI
) : BaseRetrofitDataSource(), NetworkDataSource {

    context(Raise<NetworkDSError>)
    override suspend fun getDevices(): DevicesResponse {
        return executeCall { deviceApi.getDevices() }
    }

    context (Raise<NetworkDSError>)
    suspend fun getDevicesWithException(): DevicesResponse =
        executeCall { throw HttpException(Response.error<DevicesResponse>(404, "".toResponseBody())) }
}
