package features.auth.data.source

import android.content.SharedPreferences
import features.auth.data.AuthTokenDataSource
import kotlinx.serialization.json.Json

actual class AuthTokenDataSourceImpl(
    private val sharedPreferences: SharedPreferences,
    private val json: Json,
) : AuthTokenDataSource {

    override fun isUserAuthorized(): Boolean {
        return getAccessToken() != null
    }

    override fun getAccessToken(): String? {
        if (getAuthDataObject()?.accessToken.isNullOrEmpty()) {
            return null
        }
        return getAuthDataObject()?.accessToken
    }

    override fun getRefreshToken(): String? {
        if (getAuthDataObject()?.refreshToken.isNullOrEmpty()) {
            return null
        }
        return getAuthDataObject()?.refreshToken
    }

    override fun setAccessToken(token: String) {
        setAuthDataObject(accessToken = token)
    }

    override fun setRefreshToken(token: String) {
        setAuthDataObject(refreshToken = token)
    }

    private fun setAuthDataObject(accessToken: String? = null, refreshToken: String? = null) {
        if (accessToken == null && refreshToken == null) {
            sharedPreferences.edit()
                .remove(AUTH_DATA_OBJECT_KEY)
                .apply()
            return
        }
        val authData = getAuthDataObject()
        val updatedAuthDataObject = authData?.copy(
            accessToken = accessToken ?: authData.accessToken,
            refreshToken = refreshToken ?: authData.refreshToken,
        ) ?: AuthDataModel(
            accessToken = accessToken.orEmpty(),
            refreshToken = refreshToken.orEmpty(),
        )
        val stringJson = json.encodeToString(AuthDataModel.serializer(), updatedAuthDataObject)

        sharedPreferences.edit()
            .putString(AUTH_DATA_OBJECT_KEY, stringJson)
            .apply()
    }

    private fun getAuthDataObject(): AuthDataModel? {
        return sharedPreferences.getString(AUTH_DATA_OBJECT_KEY, null)?.let {
            try {
                json.decodeFromString(AuthDataModel.serializer(), it)
            } catch (e: Throwable) {
                e.printStackTrace()
                null
            }
        }
    }

    private companion object {
        const val AUTH_DATA_OBJECT_KEY = "cached_auth_data"
    }
}