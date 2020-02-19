package fr.racomach.zigweelo.extension

import assertk.Assert
import assertk.assertions.support.expected
import assertk.assertions.support.show
import fr.racomach.zigweelo.utils.Try
import fr.racomach.zigweelo.utils.exceptionOrNull
import fr.racomach.zigweelo.utils.getOrNull
import fr.racomach.zigweelo.utils.isSuccess
import java.io.PrintWriter
import java.io.StringWriter

fun <T> Assert<Try<T>>.isSuccess(): Assert<T> = transform { actual ->
    if (actual.isSuccess()) {
        @Suppress("UNCHECKED_CAST")
        actual.getOrNull() as T
    } else {
        expected(
            "success but was failure:${showError(
                actual.exceptionOrNull()!!
            )}"
        )
    }
}

fun <T> Assert<Try<T>>.isFail(): Assert<Throwable> = transform { actual ->
    if (actual.isSuccess()) {
        expected("expected a failure but was a success: ${actual.getOrNull()}")
    } else {
        actual.exceptionOrNull()!!
    }
}

fun showError(e: Throwable): String {
    val stackTrace = StringWriter()
    e.printStackTrace(PrintWriter(stackTrace))
    return "${show(e)}\n$stackTrace"
}