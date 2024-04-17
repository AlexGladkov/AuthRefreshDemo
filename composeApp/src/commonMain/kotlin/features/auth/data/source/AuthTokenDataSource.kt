package features.auth.data

interface AuthTokenDataSource {
    fun isUserAuthorized(): Boolean
    fun getAccessToken(): String?
    fun getRefreshToken(): String?
    fun setAccessToken(token: String)
    fun setRefreshToken(token: String)
}