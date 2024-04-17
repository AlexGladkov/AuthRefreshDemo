package features.auth.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Suppress("KtorNetworkModelInspection")
@Serializable
internal data class KtorRefreshTokenResponse(
    @SerialName(value = "access-token") val accessToken: String?,
    @SerialName(value = "refresh-token") val refreshToken: String?,
)