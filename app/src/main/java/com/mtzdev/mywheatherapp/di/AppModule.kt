package com.mtzdev.mywheatherapp.di

import android.net.http.HttpResponseCache
import com.mtzdev.mywheatherapp.BuildConfig
import com.mtzdev.mywheatherapp.commons.WEATHER_API_KEY
import com.mtzdev.mywheatherapp.commons.WEATHER_BASE_URL
import com.mtzdev.mywheatherapp.commons.WEATHER_GEOLOACTION_CLIENT
import com.mtzdev.mywheatherapp.commons.WEATHER_HTTP_CLIENT
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.android.Android
import io.ktor.client.engine.android.AndroidEngineConfig
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module

val appModule = module {
    single<String>(named(WEATHER_API_KEY)) {
        BuildConfig.WEATHER_API_KEY
    }

    single {
        named(WEATHER_HTTP_CLIENT)
        HttpClient(Android) {
            defaultConfing()
            defaultRequest {
                url(WEATHER_BASE_URL)
                header(HttpHeaders.ContentType, ContentType.Application.Json)
            }
        }
    }

    single {
        named(WEATHER_GEOLOACTION_CLIENT)
        HttpClient(Android) {
            defaultConfing()
            defaultRequest {
                url(WEATHER_BASE_URL)
                header(HttpHeaders.ContentType, ContentType.Application.Json)
            }
        }
    }
}

private fun HttpClientConfig<AndroidEngineConfig>.defaultConfing() {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            isLenient = true
        })
    }
    install(Logging) {
        level = LogLevel.ALL
    }
}