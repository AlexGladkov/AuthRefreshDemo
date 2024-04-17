package core.ktor

import core.ktor.engine.HttpEngineFactory
import core.ktor.features.AuthRefreshFeature
import features.auth.data.AuthErrorDataSource
import features.auth.data.AuthTokenDataSource
import features.auth.data.source.KtorRefreshTokenDataSource
import io.ktor.client.*
import io.ktor.client.engine.*
import org.kodein.di.*

val ktorModule = DI.Module(
    name = "common:ktorModule",
    init = {
        bind<HttpEngineFactory>() with singleton { HttpEngineFactory() }
        bind<KtorRefreshTokenDataSource>() with provider {
            KtorRefreshTokenDataSource()
        }
        bind<HttpClient>() with singleton {
            buildHttpClient(
                engine = instance<HttpEngineFactory>().createEngine(),
                tokenDataSource = instance(),
                authTokenDataSource = instance(),
                authErrorDataSource = instance(),
            )
        }
    },
)

@Suppress("LongParameterList")
private fun buildHttpClient(
    engine: HttpClientEngineFactory<HttpClientEngineConfig>,
    tokenDataSource: KtorRefreshTokenDataSource,
    authTokenDataSource: AuthTokenDataSource,
    authErrorDataSource: AuthErrorDataSource
) = HttpClient(engine) {

    install(AuthRefreshFeature) {
        this.refreshTokenDataSource = tokenDataSource
        this.authTokenDataSource = authTokenDataSource
        this.authErrorDataSource = authErrorDataSource
        this.maxAttempts = 3
    }
}
