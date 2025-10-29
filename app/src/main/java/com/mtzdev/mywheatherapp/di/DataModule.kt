package com.mtzdev.mywheatherapp.di

import com.mtzdev.mywheatherapp.commons.WEATHER_API_KEY
import com.mtzdev.mywheatherapp.commons.WEATHER_HTTP_CLIENT
import com.mtzdev.mywheatherapp.data.WeatherDataDataSource
import com.mtzdev.mywheatherapp.data.WeatherGeoDataSource
import com.mtzdev.mywheatherapp.data.remote.WeatherDataDataSourceRemote
import com.mtzdev.mywheatherapp.data.remote.WeatherGeoDataSourceRemote
import com.mtzdev.mywheatherapp.data.repository.WeatherDataRepositoryData
import com.mtzdev.mywheatherapp.data.repository.WeatherGeoRepositoryData
import com.mtzdev.mywheatherapp.data.services.WeatherDataService
import com.mtzdev.mywheatherapp.domain.repository.WeatherDataRepository
import com.mtzdev.mywheatherapp.domain.repository.WeatherGeoRepository
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

val dataModule = module {
    single {
        WeatherDataService(
            client = get(named(WEATHER_HTTP_CLIENT)),
            apiKey = get(named(WEATHER_API_KEY))
        )
    }
    singleOf(::WeatherGeoDataSourceRemote) bind WeatherGeoDataSource::class
    singleOf(::WeatherGeoRepositoryData) bind WeatherGeoRepository::class
    singleOf(::WeatherDataDataSourceRemote) bind WeatherDataDataSource::class
    singleOf(::WeatherDataRepositoryData) bind WeatherDataRepository::class
}