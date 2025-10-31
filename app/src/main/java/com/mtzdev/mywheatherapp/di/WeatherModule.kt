package com.mtzdev.mywheatherapp.di

import com.mtzdev.mywheatherapp.commons.WEATHER_API_KEY
import com.mtzdev.mywheatherapp.commons.WEATHER_HTTP_CLIENT
import com.mtzdev.mywheatherapp.data.location.LocationProvider
import com.mtzdev.mywheatherapp.data.mapper.LocationMapper
import com.mtzdev.mywheatherapp.data.mapper.WeatherMapper
import com.mtzdev.mywheatherapp.data.remote.datasource.GeocodingRemoteDataSource
import com.mtzdev.mywheatherapp.data.remote.datasource.WeatherRemoteDataSource
import com.mtzdev.mywheatherapp.data.repository.LocationRepositoryImpl
import com.mtzdev.mywheatherapp.data.repository.WeatherRepositoryImpl
import com.mtzdev.mywheatherapp.domain.repository.LocationRepository
import com.mtzdev.mywheatherapp.domain.repository.WeatherRepository
import com.mtzdev.mywheatherapp.domain.usecase.GetCurrentLocationUseCase
import com.mtzdev.mywheatherapp.domain.usecase.GetCurrentWeatherByCoordinatesUseCase
import com.mtzdev.mywheatherapp.domain.usecase.SearchLocationUseCase
import com.mtzdev.mywheatherapp.ui.weather.WeatherScreenModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * T087-T095: Koin module for weather feature dependencies.
 *
 * Provides:
 * - Data sources (single): LocationProvider, WeatherRemoteDataSource, GeocodingRemoteDataSource
 * - Mappers (single): WeatherMapper, LocationMapper
 * - Repositories (single): WeatherRepositoryImpl, LocationRepositoryImpl
 * - Use cases (factory): GetCurrentLocationUseCase, GetCurrentWeatherByCoordinatesUseCase, SearchLocationUseCase
 * - ScreenModel (factory): WeatherScreenModel
 */
val weatherModule = module {

    // T088: LocationProvider - single with androidContext
    single { LocationProvider(androidContext()) }

    // T089: WeatherRemoteDataSource - single with named HttpClient and API key
    single {
        WeatherRemoteDataSource(
            client = get(named(WEATHER_HTTP_CLIENT)),
            apiKey = get(named(WEATHER_API_KEY))
        )
    }

    // T090: GeocodingRemoteDataSource - single with named HttpClient and API key
    single {
        GeocodingRemoteDataSource(
            httpClient = get(named(WEATHER_HTTP_CLIENT)),
            apiKey = get(named(WEATHER_API_KEY))
        )
    }

    // T091: Mappers - singleOf
    singleOf(::WeatherMapper)
    singleOf(::LocationMapper)

    // T092: WeatherRepositoryImpl - singleOf with bind
    singleOf(::WeatherRepositoryImpl) bind WeatherRepository::class

    // T093: LocationRepositoryImpl - singleOf with bind
    singleOf(::LocationRepositoryImpl) bind LocationRepository::class

    // T094: Use cases - factoryOf for all three
    factoryOf(::GetCurrentLocationUseCase)
    factoryOf(::GetCurrentWeatherByCoordinatesUseCase)
    factoryOf(::SearchLocationUseCase)

    // T095: WeatherScreenModel - factoryOf
    factoryOf(::WeatherScreenModel)
}
