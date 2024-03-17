package com.example.bletest.datalayer.network

import com.google.gson.annotations.SerializedName

data class DevicesResponse(
    @SerializedName("devices")
    val devices: List<DeviceResponseItem>
)


data class DeviceResponseItem(
    @SerializedName("macAddress")
    val macAddress: String?,
    @SerializedName("model")
    val model: String?,
    @SerializedName("product")
    val product: String?,
    @SerializedName("firmwareVersion")
    val firmwareVersion: String?,
    @SerializedName("serial")
    val serial: String?,
    @SerializedName("installationMode")
    val installationMode: String?,
    @SerializedName("brakeLight")
    val brakeLight: String?,
    @SerializedName("lightMode")
    val lightMode: String?,
    @SerializedName("lightAuto")
    val lightAuto: String?,
    @SerializedName("lightValue")
    val lightValue: Int?
)