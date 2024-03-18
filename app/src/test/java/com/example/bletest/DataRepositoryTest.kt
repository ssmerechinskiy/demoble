package com.example.bletest

import arrow.core.Either
import arrow.core.raise.Raise
import arrow.core.raise.either
import com.example.bletest.datalayer.db.DBDataSource
import com.example.bletest.datalayer.db.DbDSError
import com.example.bletest.datalayer.network.DeviceResponseItem
import com.example.bletest.datalayer.network.DevicesResponse
import com.example.bletest.datalayer.network.NetworkDSError
import com.example.bletest.datalayer.network.NetworkDataSource
import com.example.bletest.datalayer.repository.DataRepositoryImpl
import com.example.bletest.datalayer.repository.DeviceModel
import com.example.bletest.datalayer.repository.toDomain
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class DataRepositoryTest {

    @Test
    fun getFromNetwork_checkInDB() = runTest{

        class TestNetworkDataSource : NetworkDataSource {
            context(Raise<NetworkDSError>)
            override suspend fun getDevices(): DevicesResponse = apiResponseStub
        }
        val networkDataSource :  NetworkDataSource = TestNetworkDataSource()

        val dbDataSource : DBDataSource = TestDB()
        val dispatcher = StandardTestDispatcher(testScheduler)

        val repository = DataRepositoryImpl(dispatcher, networkDataSource, dbDataSource)
        either { repository.getDeviceModels() }

        when(val dbData = either { dbDataSource.getAllDevices() }) {
            is Either.Left -> assert(false)
            is Either.Right -> assertEquals(apiResponseStub.devices.size, dbData.value.size)
        }
    }

    @Test
    fun getOnlyFromDB() = runTest{

        class TestNetworkDataSource : NetworkDataSource {
            context(Raise<NetworkDSError>)
            override suspend fun getDevices(): DevicesResponse = DevicesResponse(emptyList())
        }
        val networkDataSource :  NetworkDataSource = TestNetworkDataSource()

        val dbDataSource : DBDataSource = TestDB()
        either { dbDataSource.insertAllDevices(*dbModelsStub.toTypedArray()) }

        val dispatcher = StandardTestDispatcher(testScheduler)

        val repository = DataRepositoryImpl(dispatcher, networkDataSource, dbDataSource)
        either { repository.getDeviceModels(fromCache = true) }

        when(val dbData = either { dbDataSource.getAllDevices() }) {
            is Either.Left -> assert(false)
            is Either.Right -> assertEquals(dbModelsStub.size, dbData.value.size)
        }
    }

    class TestDB() : DBDataSource {

        private val data = ArrayList<DeviceModel>()

        context(Raise<DbDSError>)
        override suspend fun getAllDevices(): List<DeviceModel> {
            return data
        }

        context(Raise<DbDSError>)
        override suspend fun findDeviceByMac(mac: String): DeviceModel? {
            return data.firstOrNull {
                it.macAddress == mac
            }
        }

        context(Raise<DbDSError>)
        override suspend fun insertAllDevices(vararg devices: DeviceModel) {
            data.addAll(devices)
        }

        context(Raise<DbDSError>)
        override suspend fun deleteDevice(device: DeviceModel) {
            data.firstOrNull {
                it.macAddress == device.macAddress
            }?.let {
                data.remove(it)
            }
        }

        context(Raise<DbDSError>)
        override suspend fun deleteAllDevices() {
            data.clear()
        }

    }

    @Test
    fun checkToDomainConverter() {
        val dto = DeviceResponseItem(
            macAddress = "address1",
            model = "model1",
            product = "product1",
            firmwareVersion = "firmwareVersion1",
            serial = "serial1",
            installationMode = "helmet",
            brakeLight = "false",
            lightMode = "OFF",
            lightAuto = "false",
            lightValue = 0
        )
        val domain = dto.toDomain()
        assertEquals(dto.macAddress, domain.macAddress)
        assertEquals(dto.model, domain.model)
        assertEquals(dto.product, domain.product)
        assertEquals(dto.firmwareVersion, domain.firmwareVersion)
        assertEquals(dto.serial, domain.serial)
        assert(dto.installationMode.equals(domain.installationMode.descr))
        assert(dto.brakeLight.equals(domain.brakeLight.descr))
        assert(dto.lightMode.equals(domain.lightMode.descr))
        assert(dto.lightAuto.equals(domain.lightAuto.descr))
        assertEquals(dto.lightValue, domain.lightValue)
    }
}
