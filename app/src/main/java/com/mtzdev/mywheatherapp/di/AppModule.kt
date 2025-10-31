package com.mtzdev.mywheatherapp.di

import com.mtzdev.mywheatherapp.commons.PermissionManager
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val appModule = module {
    includes(apiModule, dataModule, viewModelModule)

    // Utilities
    singleOf(::PermissionManager)
}