package features.auth.data.errors

import io.ktor.client.statement.*

sealed class BadResponse(
    val originResponse: HttpResponse,
    message: String? = null,
    cause: Throwable? = null,
) : Exception(message, cause) {

    @Suppress("LongParameterList")
    class ApiError(
        originResponse: HttpResponse,
        val url: String,
        val method: String,
        val errors: List<Error>,
        val originalResponseText: String,
        val responseStatusCode: Int,
    ) : BadResponse(
        originResponse = originResponse,
        message = """
            | > status code: $responseStatusCode
            | > method: $method
            | > url: $url
        """.trimIndent(),
    ) {

        data class Error(val message: String?, val stringCode: String)

        override fun toString(): String {
            val errorsDescription = errors.takeIf { it.isNotEmpty() }
                ?.joinToString(separator = "\n") {
                    """
                        | > code: ${it.stringCode}
                        | > message: ${it.message}
                    """.trimMargin()
                }
                ?: originalResponseText

            return """
                |
                |━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                |Api response error!
                | > status code: $responseStatusCode
                |$errorsDescription
                |━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                |
            """.trimMargin()
        }
    }

    class Unknown(
        originResponse: HttpResponse,
        val responseCode: Int,
        val responseBody: String,
        val originalException: Exception, // Original error of parsing ApiError model, may indicate to wrong json path, val traceUrl: kotlin.String?){}
    ) : BadResponse(
        originResponse = originResponse,
        message = """
        | > status code: $responseCode
        """.trimIndent(),
        cause = originalException,
    )
}