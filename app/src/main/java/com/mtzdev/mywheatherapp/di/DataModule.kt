package com.mtzdev.mywheatherapp.di

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.mtzdev.mywheatherapp.commons.WEATHER_API_KEY
import com.mtzdev.mywheatherapp.commons.WEATHER_GEOLOACTION_CLIENT
import com.mtzdev.mywheatherapp.commons.WEATHER_HTTP_CLIENT
import com.mtzdev.mywheatherapp.data.LocationDataSource
import com.mtzdev.mywheatherapp.data.WeatherDataDataSource
import com.mtzdev.mywheatherapp.data.WeatherGeoDataSource
import com.mtzdev.mywheatherapp.data.local.LocationDataSourceLocal
import com.mtzdev.mywheatherapp.data.remote.WeatherDataDataSourceRemote
import com.mtzdev.mywheatherapp.data.remote.WeatherGeoDataSourceRemote
import com.mtzdev.mywheatherapp.data.repository.LocationRepositoryData
import com.mtzdev.mywheatherapp.data.repository.WeatherDataRepositoryData
import com.mtzdev.mywheatherapp.data.repository.WeatherGeoRepositoryData
import com.mtzdev.mywheatherapp.data.services.WeatherDataService
import com.mtzdev.mywheatherapp.data.services.WeatherGeoService
import com.mtzdev.mywheatherapp.domain.repository.LocationRepository
import com.mtzdev.mywheatherapp.domain.repository.WeatherDataRepository
import com.mtzdev.mywheatherapp.domain.repository.WeatherGeoRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

val dataModule = module {
    // Weather services
    single {
        WeatherGeoService(
            client = get(named(WEATHER_GEOLOACTION_CLIENT)),
            apiKey = get(named(WEATHER_API_KEY))
        )
    }
    single {
        WeatherDataService(
            client = get(named(WEATHER_HTTP_CLIENT)),
            apiKey = get(named(WEATHER_API_KEY))
        )
    }

    // Location services
    single<FusedLocationProviderClient> {
        LocationServices.getFusedLocationProviderClient(androidContext())
    }

    // Data sources
    singleOf(::WeatherGeoDataSourceRemote) bind WeatherGeoDataSource::class
    singleOf(::WeatherDataDataSourceRemote) bind WeatherDataDataSource::class
    singleOf(::LocationDataSourceLocal) bind LocationDataSource::class

    // Repositories
    singleOf(::WeatherGeoRepositoryData) bind WeatherGeoRepository::class
    singleOf(::WeatherDataRepositoryData) bind WeatherDataRepository::class
    singleOf(::LocationRepositoryData) bind LocationRepository::class
}