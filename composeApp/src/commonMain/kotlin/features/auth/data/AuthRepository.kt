package features.auth.data

import features.auth.data.errors.AuthError
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    fun cleanAuthData()

    fun setAccessToken(token: String)
    fun setRefreshToken(token: String)

    fun getAccessToken(): String?
    fun getRefreshToken(): String?
    fun isUserAuthorized(): Boolean
    fun observeAuthErrors(): Flow<AuthError>
}