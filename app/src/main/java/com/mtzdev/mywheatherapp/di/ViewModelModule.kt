package com.mtzdev.mywheatherapp.di

import com.mtzdev.mywheatherapp.domain.usecase.GetWeatherDataUseCase
import com.mtzdev.mywheatherapp.domain.usecase.GetWeatherGeoDataUseCase
import com.mtzdev.mywheatherapp.ui.screen.home.HomeScreenModel
import com.mtzdev.mywheatherapp.ui.screen.weatherdata.WeatherDataScreenModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val viewModelModule = module {
    factoryOf(::GetWeatherGeoDataUseCase)
    factoryOf(::GetWeatherDataUseCase)
    factoryOf(::HomeScreenModel)
    factoryOf(::WeatherDataScreenModel)
}