package fr.racomach.zigweelo.extension

import fr.racomach.zigweelo.utils.Try
import retrofit2.Response

/**
 * Converts a Retrofit [Response] to an [Result] instance.
 */
fun <T> Response<T>.toTry(): Try<T> =
    if (isSuccessful)
        Try.Success(body()!!)
    else
        Try.Failure(Exception(errorBody()?.string() ?: "Unknown error"))