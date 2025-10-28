package com.mtzdev.mywheatherapp.di

import org.koin.dsl.module

val appModule = module {
    includes(apiModule, dataModule, viewModelModule)
}