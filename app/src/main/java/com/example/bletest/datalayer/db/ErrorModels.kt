package com.example.bletest.datalayer.db

sealed class DbDSError(open val message : String = "")

data class GeneralError(
    override val message : String
) : DbDSError(message)