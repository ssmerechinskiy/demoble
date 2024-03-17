package com.example.bletest.datalayer.repository

import com.example.bletest.datalayer.db.DbDSError
import com.example.bletest.datalayer.network.NetworkDSError

sealed class DataLayerError {
    data class NetworkError(val error: NetworkDSError) : DataLayerError()
    data class StorageError(val error: DbDSError) : DataLayerError()
}