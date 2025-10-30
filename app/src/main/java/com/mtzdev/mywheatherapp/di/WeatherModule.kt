package com.mtzdev.mywheatherapp.di

import org.koin.dsl.module

/**
 * Koin module for weather feature dependencies.
 * Provides weather-related repositories, use cases, and mappers.
 *
 * TODO: Populate with following dependencies in Phase 9:
 * - WeatherRepository implementation
 * - LocationRepository implementation
 * - GetCurrentWeatherByCoordinatesUseCase
 * - SearchLocationUseCase
 * - WeatherMapper
 * - LocationMapper
 */
val weatherModule = module {
    // TODO: Add repository implementations
    // single<WeatherRepository> { WeatherRepositoryImpl(get(), get()) }
    // single<LocationRepository> { LocationRepositoryImpl(get(), get()) }

    // TODO: Add use cases
    // factory { GetCurrentWeatherByCoordinatesUseCase(get()) }
    // factory { SearchLocationUseCase(get()) }

    // TODO: Add mappers
    // single { WeatherMapper }
    // single { LocationMapper }
}
