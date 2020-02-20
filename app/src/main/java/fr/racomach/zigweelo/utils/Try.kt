package fr.racomach.zigweelo.utils

/**
 * The `Try` type represents a computation that may either result in an exception, or return a
 * successfully computed value.
 *
 * Inspired by:
 * https://arrow-kt.io/docs/arrow/core/try/
 */
sealed class Try<out V> {
    data class Failure<out V>(val exception: Throwable) : Try<V>()
    data class Success<out V>(val result: V) : Try<V>()
}

fun <V> Try<V>.isSuccess() = this is Try.Success

fun <V> Try<V>.getOrNull() = when (this) {
    is Try.Failure -> null
    is Try.Success -> result
}

fun <V> Try<V>.exceptionOrNull() = when (this) {
    is Try.Failure -> exception
    is Try.Success -> null
}

inline infix fun <V, V2> Try<V>.flatMap(f: (V) -> Try<V2>): Try<V2> = when (this) {
    is Try.Failure -> Try.Failure(exception)
    is Try.Success -> f(result)
}

inline fun <V, V2> Try<V>.fold(ifFailure: (Throwable) -> V2, ifSuccess: (V) -> V2): V2 =
    when (this) {
        is Try.Failure -> ifFailure(exception)
        is Try.Success -> ifSuccess(result)
    }

inline infix fun <V, V2> Try<V>.map(f: (V) -> V2): Try<V2> = flatMap { Try.Success(f(it)) }

inline fun <T, R> T.runTry(block: T.() -> R): Try<R> {
    return try {
        Try.Success(block())
    } catch (e: Throwable) {
        Try.Failure(e)
    }
}
