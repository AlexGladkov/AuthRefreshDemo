package features.auth.data.errors

sealed class AuthError : Exception() {
    data object InvalidAccessToken : AuthError()
    data object InvalidRefreshToken : AuthError()
    data object OAuthServiceUnavailable : AuthError()
    data object RefreshTokenIsNotSpecified : AuthError()
    data class UnknownAuthError(val responseText: String) : AuthError()
}