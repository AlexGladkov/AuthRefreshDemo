package core.ktor.engine

import io.ktor.client.engine.*
import io.ktor.client.engine.okhttp.*
import okhttp3.Dispatcher

actual open class HttpEngineFactory actual constructor() {

    actual open fun createEngine(): HttpClientEngineFactory<HttpClientEngineConfig> =
        OkHttp.config {
            config {
                retryOnConnectionFailure(true)
                dispatcher(
                    Dispatcher().apply {
                        this.maxRequestsPerHost = this.maxRequests
                    },
                )
            }
        }
}