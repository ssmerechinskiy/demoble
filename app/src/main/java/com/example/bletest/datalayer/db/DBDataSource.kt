package com.example.bletest.datalayer.db

import arrow.core.raise.Raise
import arrow.core.raise.catch
import com.example.bletest.datalayer.repository.DeviceModel
import javax.inject.Inject

interface DBDataSource {

    context (Raise<DbDSError>)
    suspend fun getAllDevices(): List<DeviceModel>

    context (Raise<DbDSError>)
    suspend fun findDeviceByMac(mac: String): DeviceModel?

    context (Raise<DbDSError>)
    suspend fun insertAllDevices(vararg devices: DeviceModel)

    context (Raise<DbDSError>)
    suspend fun deleteDevice(device: DeviceModel)

    context (Raise<DbDSError>)
    suspend fun deleteAllDevices()
}

class DBDataSourceImpl @Inject constructor (
    private val db : AppDatabase
) : DBDataSource  {

    context (Raise<DbDSError>)
    override suspend fun getAllDevices(): List<DeviceModel> = catch({
        db.deviceDao().getAll()
    }) { raise(GeneralError(it.message ?: "message is empty")) }

//    context (Raise<DbDSError>)
//    override suspend fun getAllDevices(): List<DeviceModel> = raise(GeneralError("database error"))

    context (Raise<DbDSError>)
    override suspend fun findDeviceByMac(mac: String): DeviceModel? = catch({
        db.deviceDao().findByMac(mac)
    }) { raise(GeneralError(it.message ?: "message is empty")) }

    context (Raise<DbDSError>)
    override suspend fun insertAllDevices(vararg devices: DeviceModel) = catch({
        db.deviceDao().insertAll(*devices)
    }) { raise(GeneralError(it.message ?: "message is empty")) }

    context (Raise<DbDSError>)
    override suspend fun deleteDevice(device: DeviceModel) = catch({
        db.deviceDao().delete(device)
    }) { raise(GeneralError(it.message ?: "message is empty")) }

    context (Raise<DbDSError>)
    override suspend fun deleteAllDevices() = catch({
        db.deviceDao().deleteAll()
    }) { raise(GeneralError(it.message ?: "message is empty")) }
}