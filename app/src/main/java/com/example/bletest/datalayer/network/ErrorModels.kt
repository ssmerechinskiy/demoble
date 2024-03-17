package com.example.bletest.datalayer.network

import retrofit2.Response

sealed class NetworkDSError(open val code : Int = 0, open val message : String = "")

data class EmptyBodyError(
    override val code : Int
) : NetworkDSError(code, "Response body is empty")

data object NoInternetError : NetworkDSError(0, "Internet connection doesn`t exist")

data class GeneralError(
    override val code : Int,
    override val message : String,
    val response: Response<*>? = null
) : NetworkDSError(code, message)

data class ApiError(
    override val code : Int,
    val json : String,
) : NetworkDSError(code, json)