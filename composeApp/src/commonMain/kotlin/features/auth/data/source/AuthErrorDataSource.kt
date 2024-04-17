package features.auth.data

import features.auth.data.errors.AuthError
import kotlinx.coroutines.flow.Flow

interface AuthErrorDataSource {
    fun emmitError(error: AuthError)
    fun observeError(): Flow<AuthError>
}