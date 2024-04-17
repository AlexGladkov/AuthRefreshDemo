package features.auth.data.source

import features.auth.data.AuthTokenDataSource
import ru.leroymerlin.mobile.auth.data.data_source.keychain.KeychainHelper

actual class AuthTokenDataSourceImpl : AuthTokenDataSource {

    private val keychainHelper = KeychainHelper(SERVICE_NAME)

    override fun isUserAuthorized(): Boolean {
        val refreshToken = getRefreshToken()
        return !refreshToken.isNullOrBlank()
    }

    override fun getAccessToken(): String? {
        return keychainHelper.string(ACCESS_TOKEN_KEY)
    }

    override fun getRefreshToken(): String? = keychainHelper.string(REFRESH_TOKEN_KEY)

    override fun setAccessToken(token: String) {
        if (token.isNotBlank()) {
            keychainHelper.set(ACCESS_TOKEN_KEY, token)
        } else {
            keychainHelper.deleteObject(ACCESS_TOKEN_KEY)
        }
    }

    override fun setRefreshToken(token: String) {
        if (token.isNotBlank()) {
            keychainHelper.set(REFRESH_TOKEN_KEY, token)
        } else {
            keychainHelper.deleteObject(REFRESH_TOKEN_KEY)
        }
    }

    companion object {

        private const val SERVICE_NAME = "ru.leroymerlin.keychain.token"
        private const val ACCESS_TOKEN_KEY = "accessTokenRaw"
        private const val REFRESH_TOKEN_KEY = "refreshTokenRaw"
    }
}