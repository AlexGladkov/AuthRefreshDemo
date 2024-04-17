package core.ktor.engine

import io.ktor.client.engine.*

expect open class HttpEngineFactory() {

    fun createEngine(): HttpClientEngineFactory<HttpClientEngineConfig>
}