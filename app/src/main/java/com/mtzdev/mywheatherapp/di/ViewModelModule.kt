package com.mtzdev.mywheatherapp.di

import com.mtzdev.mywheatherapp.domain.usecase.GetCurrentLocationUseCase
import com.mtzdev.mywheatherapp.domain.usecase.GetWeatherDataUseCase
import com.mtzdev.mywheatherapp.domain.usecase.GetWeatherGeoDataUseCase
import com.mtzdev.mywheatherapp.ui.screen.home.HomeScreenModel
import com.mtzdev.mywheatherapp.ui.screen.weatherdata.WeatherDataScreenModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val viewModelModule = module {
    // Use cases
    factoryOf(::GetWeatherGeoDataUseCase)
    factoryOf(::GetWeatherDataUseCase)
    factoryOf(::GetCurrentLocationUseCase)

    // Screen models
    factoryOf(::HomeScreenModel)
    factoryOf(::WeatherDataScreenModel)
}