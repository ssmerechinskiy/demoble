package com.example.bletest.datalayer.repository

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "device")
data class DeviceModel(
    @PrimaryKey
    @ColumnInfo(name = "mac_address")
    val macAddress: String,
    @ColumnInfo(name = "model")
    val model: String,
    @ColumnInfo(name = "product")
    val product: String,
    @ColumnInfo(name = "firmware_version")
    val firmwareVersion: String,
    @ColumnInfo(name = "serial")
    val serial: String,
    @ColumnInfo(name = "installation_mode")
    val installationMode: InstallationMode,
    @ColumnInfo(name = "brake_light")
    val brakeLight: Switch,
    @ColumnInfo(name = "light_mode")
    val lightMode: LightMode,
    @ColumnInfo(name = "light_auto")
    val lightAuto: Switch,
    @ColumnInfo(name = "light_value")
    val lightValue: Int
)

enum class InstallationMode(override val descr: String) : Description {
    NONE("none"), HELMET("helmet"), SEAT("seat")
}

enum class LightMode(override val descr: String) : Description {
    NONE("NONE"), WARNING("WARNING"), BOTH("BOTH"), OFF("OFF")
}

enum class Switch(override val descr: String) : Description {
    NONE("none"), TRUE("true"), FALSE("false")
}

interface Description {
    val descr : String
}

fun <T: Description> getByDescription(input: Array<T>, descr: String?, onNullable: () -> T) : T {
    if (descr == null) return onNullable()
    var res : T? = input.firstOrNull {
        it.descr.equals(descr, ignoreCase = true)
    }
    return res ?: onNullable()
}