package features.auth

import features.auth.data.AuthErrorDataSource
import features.auth.data.AuthRepository
import features.auth.data.AuthRepositoryImpl
import features.auth.data.AuthTokenDataSource
import features.auth.data.source.AuthErrorDataSourceImpl
import features.auth.data.source.AuthTokenDataSourceImpl
import org.kodein.di.*

val authModule = DI.Module(
    name = "authModule",
    init = {
        // DataSource
        bindSingleton { new(::AuthErrorDataSourceImpl) }

        // External DataSource implementations
        bindSingleton<AuthErrorDataSource> { instance<AuthErrorDataSourceImpl>() }

        // Repository
        bindSingleton<AuthRepository> { new(::AuthRepositoryImpl) }
    },
)