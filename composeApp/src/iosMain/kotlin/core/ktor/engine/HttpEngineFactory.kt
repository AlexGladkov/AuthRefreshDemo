package core.ktor.engine

import io.ktor.client.engine.*
import io.ktor.client.engine.darwin.*

actual open class HttpEngineFactory actual constructor() {

    actual fun createEngine( ): HttpClientEngineFactory<HttpClientEngineConfig> = Darwin
}