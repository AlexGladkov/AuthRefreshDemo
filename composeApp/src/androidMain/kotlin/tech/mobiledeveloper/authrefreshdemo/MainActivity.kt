package tech.mobiledeveloper.authrefreshdemo

import App
import DIHolder
import LoginScreen
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import features.auth.data.AuthRepository
import features.auth.data.errors.AuthError
import features.auth.data.source.AuthTokenDataSourceImpl
import kotlinx.serialization.json.Json
import org.kodein.di.instance

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DIHolder.init(AuthTokenDataSourceImpl(
            sharedPreferences = getSharedPreferences(getString(R.string.app_name), 0),
            json = Json
        ))
        val authRepository: AuthRepository = DIHolder.di.instance()
        setContent {
            val error by authRepository.observeAuthErrors().collectAsState(null)
            
            // Here we handle login refresh error
            when (error) {
                is AuthError.InvalidRefreshToken -> LoginScreen()
                else -> App()
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}