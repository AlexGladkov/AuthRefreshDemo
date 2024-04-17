package features.auth.data.source

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class AuthDataModel(
    @SerialName("access-token") val accessToken: String,
    @SerialName("refresh-token") val refreshToken: String,
)