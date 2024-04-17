package core.ktor.features

import features.auth.data.AuthErrorDataSource
import features.auth.data.AuthTokenDataSource
import features.auth.data.errors.AuthError
import features.auth.data.errors.BadResponse
import features.auth.data.model.KtorRefreshTokenResponse
import features.auth.data.source.KtorRefreshTokenDataSource
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

internal class AuthRefreshFeature(
    private val refreshTokenDataSource: KtorRefreshTokenDataSource,
    private val authTokenDataSource: AuthTokenDataSource,
    private val authErrorDataSource: AuthErrorDataSource,
    private val json: Json
) {

    private val mutex = Mutex()

    @Suppress("LongParameterList")
    class Config(
        var refreshTokenDataSource: KtorRefreshTokenDataSource? = null,
        var authTokenDataSource: AuthTokenDataSource? = null,
        var authErrorDataSource: AuthErrorDataSource? = null,
        var json: Json? = null,
        var maxAttempts: Int = 1
    )

    private suspend fun callNewAccessToken(httpClient: HttpClient): String {
        val refreshToken = authTokenDataSource.getRefreshToken()

        if (refreshToken.isNullOrEmpty()) {
            authErrorDataSource.emmitError(AuthError.RefreshTokenIsNotSpecified)
            throw AuthError.RefreshTokenIsNotSpecified
        }

        try {
            val response = refreshTokenDataSource.refreshToken(
                refreshToken = refreshToken,
                httpClient = httpClient,
            )

            if (response.status == HttpStatusCode.OK) {
                val newCredential = response.bodyAsText()
                    .let { json.decodeFromString(KtorRefreshTokenResponse.serializer(), it) }

                val authToken = requireNotNull(newCredential.accessToken)
                val newRefreshToken = requireNotNull(newCredential.refreshToken)

                authTokenDataSource.setAccessToken(authToken)
                authTokenDataSource.setRefreshToken(newRefreshToken)

                return authToken
            } else {
                throw ClientRequestException(response, "Refresh token error")
            }
        } catch (throwable: Throwable) {
            if (throwable is ClientRequestException) {
                val refreshTokenAuthError = parseAuthError(
                    response = throwable.response,
                    json = json,
                )
                if (refreshTokenAuthError is AuthError) {
                    if (refreshTokenAuthError is AuthError.InvalidRefreshToken) {
                        authTokenDataSource.setAccessToken("")
                        authTokenDataSource.setRefreshToken("")
                    }
                    authErrorDataSource.emmitError(refreshTokenAuthError)
                }
                throw refreshTokenAuthError
            } else {
                throw throwable
            }
        }
    }

    companion object Feature : HttpClientPlugin<Config, AuthRefreshFeature> {

        val TokenRefreshCallAttribute = AttributeKey<Unit>("TokenRefreshCallAttribute")

        override val key: AttributeKey<AuthRefreshFeature> = AttributeKey("AuthRefreshFeature")

        override fun prepare(block: Config.() -> Unit): AuthRefreshFeature {
            val config = Config().apply(block)
            return AuthRefreshFeature(
                requireNotNull(config.refreshTokenDataSource),
                requireNotNull(config.authTokenDataSource),
                requireNotNull(config.authErrorDataSource),
                requireNotNull(config.json)
            )
        }

        @OptIn(InternalAPI::class)
        override fun install(plugin: AuthRefreshFeature, scope: HttpClient) {
            scope.plugin(HttpSend).intercept { context ->

                val call = execute(context)

                val acceptableStatus =
                    call.response.status == HttpStatusCode.Unauthorized ||
                        call.response.status == HttpStatusCode.Conflict

                // Если мы уже находится в процессе обновления токена и нам вернулась ошибка, то
                // мы не хотим уходить в рекурсию и пытаться снова обновить токен.
                val isNotTryToRefreshToken = call.request.attributes.getOrNull(TokenRefreshCallAttribute) == null

                if (acceptableStatus && isNotTryToRefreshToken) {
                    try {
                        plugin.mutex.lock()

                        val authError = parseAuthError(
                            response = call.response,
                            json = plugin.json,
                        )

                        fun rethrowError(): Nothing {
                            if (authError is AuthError) {
                                plugin.authErrorDataSource.emmitError(authError)
                            }
                            throw authError
                        }

                        when (authError) {
                            is AuthError.InvalidRefreshToken -> {
                                // На этом наши полномочия все.
                                plugin.authTokenDataSource.setAccessToken("")
                                plugin.authTokenDataSource.setRefreshToken("")
                                rethrowError()
                            }

                            is AuthError.InvalidAccessToken -> {

                                // После взятия блокировки проверяем, необходимо ли нам все еще обновить токен
                                // или он был обновлен.
                                val alreadyUpdatedToken = plugin.authTokenDataSource.getAccessToken()
                                    ?.takeIf { call.request.headers["access-token"] != it }

                                val updatedAccessToken = alreadyUpdatedToken ?: plugin.callNewAccessToken(scope)

                                val request = HttpRequestBuilder().apply {
                                    takeFromWithExecutionContext(context)
                                    headers.remove("access-token")
                                    header(
                                        "access-token",
                                        updatedAccessToken,
                                    )
                                }

                                execute(request)
                            }

                            else -> rethrowError()
                        }
                    } finally {
                        plugin.mutex.unlock()
                    }
                } else {
                    call
                }
            }
        }

        private suspend fun parseAuthError(response: HttpResponse, json: Json): Exception {
            // Work with exception here
            return AuthError.OAuthServiceUnavailable
        }
    }
}