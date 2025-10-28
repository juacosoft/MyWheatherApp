package com.mtzdev.mywheatherapp.di

import com.mtzdev.mywheatherapp.domain.usecase.GetWeatherDataUseCase
import com.mtzdev.mywheatherapp.domain.usecase.GetWeatherGeoDataUseCase
import com.mtzdev.mywheatherapp.ui.home.HomeScreenModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val viewModelModule = module {
    factoryOf(::GetWeatherGeoDataUseCase)
    factoryOf(::GetWeatherDataUseCase)
    factoryOf(::HomeScreenModel)
}