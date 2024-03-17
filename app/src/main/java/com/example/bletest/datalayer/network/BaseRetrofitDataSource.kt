package com.example.bletest.datalayer.network

import arrow.core.raise.Raise
import arrow.core.raise.catch
import retrofit2.HttpException
import retrofit2.Response


abstract class BaseRetrofitDataSource() {

    context (Raise<NetworkDSError>)
    protected suspend fun <T : Any> executeCall(call: suspend () -> Response<T>) : T = catch({
        // TODO: check internet connection before
        val response: Response<T> = call.invoke()
        if (response.isSuccessful) {
            if (response.body() != null) {
                return@catch response.body()!!
            } else {
                raise(EmptyBodyError(response.code()))
            }
        } else {
            raise(
                ApiError(
                    code = response.code(),
                    json = response.errorBody()?.string() ?: "")
            )
        }
    }) { t ->
        raise(convertThrowable(t))
    }

    private fun convertThrowable(t: Throwable) = when(t) {
        is HttpException -> GeneralError(t.code(),t.message ?: "unknown message", t.response())
        else -> GeneralError(0, t.message ?: "unknown message")
    }
}
