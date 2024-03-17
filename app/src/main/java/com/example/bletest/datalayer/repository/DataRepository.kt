package com.example.bletest.datalayer.repository

import arrow.core.raise.Raise
import arrow.core.raise.withError
import com.example.bletest.datalayer.db.DBDataSource
import com.example.bletest.datalayer.network.DeviceResponseItem
import com.example.bletest.datalayer.network.NetworkDataSource
import com.example.bletest.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface DataRepository {
    context (Raise<DataLayerError>)
    suspend fun getDeviceModels(fromCache : Boolean = false) : List<DeviceModel>

    context (Raise<DataLayerError>)
    suspend fun getDeviceModel(macAddress: String) : DeviceModel?
}

class DataRepositoryImpl @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val networkDataSource: NetworkDataSource,
    private val dbDataSource: DBDataSource
) : DataRepository {

    context (Raise<DataLayerError>)
    override suspend fun getDeviceModels(fromCache : Boolean): List<DeviceModel> {
        return withContext(ioDispatcher) {
            //load data from db or network depends on flag from cache
            val devices : List<DeviceModel> = if(fromCache) {
                withError(
                    transform = { DataLayerError.StorageError(it) }
                ) {
                    dbDataSource.getAllDevices()
                }
            } else {
                withError(
                    transform = { DataLayerError.NetworkError(it) }
                ) {
                    networkDataSource.getDevices().devices.map { it.toDomain() }
                }
            }
            //if data loads from network update db
            if (!fromCache) {
                withError(
                    transform = { DataLayerError.StorageError(it) }
                ) {
                    dbDataSource.deleteAllDevices()
                    dbDataSource.insertAllDevices(*devices.toTypedArray())
                }
            }
            return@withContext devices
        }
    }

    context(Raise<DataLayerError>)
    override suspend fun getDeviceModel(macAddress: String): DeviceModel? {
        return withError(
            transform = { DataLayerError.StorageError(it) }
        ) {
            dbDataSource.findDeviceByMac(macAddress)
        }
    }
}

fun DeviceResponseItem.toDomain() : DeviceModel {
    return DeviceModel(
        macAddress = this.macAddress ?: "",
        model = this.model ?: "",
        product = this.product ?: "",
        firmwareVersion = this.firmwareVersion ?: "",
        serial = this.serial ?: "",
        installationMode = getByDescription(
            InstallationMode.values(),
            this.installationMode
        ) { InstallationMode.NONE },
        brakeLight = getByDescription(
            Switch.values(),
            this.brakeLight
        ) { Switch.NONE },
        lightMode = getByDescription(
            LightMode.values(),
            this.lightMode
        ) { LightMode.NONE },
        lightAuto = getByDescription(
            Switch.values(),
            this.brakeLight
        ) { Switch.NONE },
        lightValue = this.lightValue ?: 0
    )
}