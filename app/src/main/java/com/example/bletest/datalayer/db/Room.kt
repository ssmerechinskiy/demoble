package com.example.bletest.datalayer.db

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RoomDatabase
import com.example.bletest.datalayer.repository.DeviceModel

@Dao
interface DeviceDao {
    @Query("SELECT * FROM device")
    suspend fun getAll(): List<DeviceModel>

    @Query("SELECT * FROM device WHERE mac_address LIKE :mac LIMIT 1")
    suspend fun findByMac(mac: String): DeviceModel?

    @Insert
    suspend fun insertAll(vararg devices: DeviceModel)

    @Delete
    suspend fun delete(device: DeviceModel)

    @Query("DELETE FROM device")
    suspend fun deleteAll()
}

@Database(entities = [DeviceModel::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun deviceDao(): DeviceDao
}