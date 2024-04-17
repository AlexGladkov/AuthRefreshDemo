package features.auth.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class KtorRefreshTokenRequest(
    @SerialName("refresh-token")
    val refreshToken: String,
)