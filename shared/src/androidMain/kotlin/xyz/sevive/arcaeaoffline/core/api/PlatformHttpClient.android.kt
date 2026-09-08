package xyz.sevive.arcaeaoffline.core.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp

actual fun platformHttpClient(): HttpClient =
    HttpClient(OkHttp) {
        resourcesApiTimeouts()
    }
