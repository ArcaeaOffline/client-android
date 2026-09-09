package xyz.sevive.arcaeaoffline.core.api

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpTimeout

/** Client construction is delegated per platform:
 * - Android uses the OkHttp engine (the official Android engine is deprecated)
 * - JVM uses CIO.
 */
expect fun platformHttpClient(): HttpClient

/**
 * Bounds shared by every request to the resources API. [HttpTimeout.requestTimeoutMillis] covers
 * the whole exchange including body transfer, so it must fit the largest published file (~1MB)
 * at weak-network throughput; connection failures surface within the much shorter connect timeout instead.
 */
internal fun HttpClientConfig<*>.resourcesApiTimeouts() {
    install(HttpTimeout) {
        connectTimeoutMillis = 15_000
        requestTimeoutMillis = 120_000
    }
}
