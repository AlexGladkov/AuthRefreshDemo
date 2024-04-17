package features.auth.data

import features.auth.data.errors.AuthError
import kotlinx.coroutines.flow.Flow

class AuthRepositoryImpl(
    private val authTokenDataSource: AuthTokenDataSource,
    private val authErrorDataSource: AuthErrorDataSource
) : AuthRepository {

    override fun cleanAuthData() {
        authTokenDataSource.setAccessToken("")
        authTokenDataSource.setRefreshToken("")
    }

    override fun getAccessToken(): String? = authTokenDataSource.getAccessToken()
    override fun setAccessToken(token: String) {
        authTokenDataSource.setAccessToken(token)
    }

    override fun getRefreshToken(): String? = authTokenDataSource.getRefreshToken()

    override fun setRefreshToken(token: String) {
        authTokenDataSource.setRefreshToken(token)
    }

    override fun isUserAuthorized(): Boolean {
        return authTokenDataSource.isUserAuthorized()
    }
    
    override fun observeAuthErrors(): Flow<AuthError> {
        return authErrorDataSource.observeError()
    }
}