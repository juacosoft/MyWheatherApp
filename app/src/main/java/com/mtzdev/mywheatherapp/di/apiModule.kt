package com.mtzdev.mywheatherapp.di

import com.mtzdev.mywheatherapp.BuildConfig
import com.mtzdev.mywheatherapp.commons.WEATHER_API_KEY
import com.mtzdev.mywheatherapp.commons.WEATHER_BASE_URL
import com.mtzdev.mywheatherapp.commons.WEATHER_GEOLOACTION_CLIENT
import com.mtzdev.mywheatherapp.commons.WEATHER_GEOLOACTION_URL
import com.mtzdev.mywheatherapp.commons.WEATHER_HTTP_CLIENT
import com.mtzdev.mywheatherapp.commons.defaultConfing
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import org.koin.core.qualifier.named
import org.koin.dsl.module

val apiModule = module {
    single<String>(named(WEATHER_API_KEY)) {
        BuildConfig.WEATHER_API_KEY
    }

    single(named(WEATHER_HTTP_CLIENT)) {
        HttpClient(Android) {
            defaultConfing()
            defaultRequest {
                url(WEATHER_BASE_URL)
                header(HttpHeaders.ContentType, ContentType.Application.Json)
            }
        }
    }

    single(named(WEATHER_GEOLOACTION_CLIENT)) {
        HttpClient(Android) {
            defaultConfing()
            defaultRequest {
                url(WEATHER_GEOLOACTION_URL)
                header(HttpHeaders.ContentType, ContentType.Application.Json)
            }
        }
    }
}