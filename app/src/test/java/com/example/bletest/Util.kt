package com.example.bletest

import com.example.bletest.datalayer.network.DeviceResponseItem
import com.example.bletest.datalayer.network.DevicesResponse
import com.example.bletest.datalayer.repository.DeviceModel
import com.example.bletest.datalayer.repository.toDomain

val apiResponseStub : DevicesResponse = DevicesResponse(
    devices = listOf(
        DeviceResponseItem(
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
        ),
        DeviceResponseItem(
            macAddress = "address2",
            model = "model2",
            product = "product2",
            firmwareVersion = "firmwareVersion2",
            serial = "serial2",
            installationMode = "helmet",
            brakeLight = "true",
            lightMode = "ON",
            lightAuto = "true",
            lightValue = 10
        )
    )
)

val dbModelsStub : List<DeviceModel> = apiResponseStub.devices.map { it.toDomain() }