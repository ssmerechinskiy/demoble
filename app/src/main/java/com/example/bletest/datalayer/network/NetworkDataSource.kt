package com.example.bletest.datalayer.network

import arrow.core.raise.Raise
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.http.GET
import javax.inject.Inject

interface NetworkDataSource {
    context (Raise<NetworkDSError>)
    suspend fun getDevices() : DevicesResponse
}

class NetworkDataSourceImpl @Inject constructor(
    private val deviceApi: DeviceAPI
) : BaseRetrofitDataSource(), NetworkDataSource {

    context (Raise<NetworkDSError>)
    override suspend fun getDevices(): DevicesResponse =
        executeCall { deviceApi.getDevices() }

}

interface DeviceAPI {
    @GET("devices")
    suspend fun getDevices() : Response<DevicesResponse>
}