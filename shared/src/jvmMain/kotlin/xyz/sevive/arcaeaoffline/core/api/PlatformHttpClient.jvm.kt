package xyz.sevive.arcaeaoffline.core.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO

actual fun platformHttpClient(): HttpClient =
    HttpClient(CIO) {
        resourcesApiTimeouts()
    }
