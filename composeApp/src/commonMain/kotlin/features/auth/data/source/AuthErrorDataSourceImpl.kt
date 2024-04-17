package features.auth.data.source

import features.auth.data.AuthErrorDataSource
import features.auth.data.errors.AuthError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

internal class AuthErrorDataSourceImpl() : AuthErrorDataSource {

    private val errorFlow = MutableSharedFlow<AuthError>()

    override fun emmitError(error: AuthError) {
        CoroutineScope(Dispatchers.Default).launch {
            errorFlow.emit(error)
        }
    }

    override fun observeError(): Flow<AuthError> {
        return errorFlow
    }
}

