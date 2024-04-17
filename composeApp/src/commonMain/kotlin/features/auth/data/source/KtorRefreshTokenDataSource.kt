package features.auth.data.source

import core.ktor.features.AuthRefreshFeature
import features.auth.data.model.KtorRefreshTokenRequest
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*

internal class KtorRefreshTokenDataSource {

    suspend fun refreshToken(
        refreshToken: String,
        httpClient: HttpClient,
    ): HttpResponse {
        return httpClient
            .post {
                setAttributes {
                    put(AuthRefreshFeature.TokenRefreshCallAttribute, Unit)
                }
                url(path = "user/v2/updateAccessToken")
                setBody(KtorRefreshTokenRequest(refreshToken = refreshToken))
            }
    }
}
