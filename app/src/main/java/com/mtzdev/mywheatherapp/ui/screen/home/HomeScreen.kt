package com.mtzdev.mywheatherapp.ui.screen.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import com.mtzdev.mywheatherapp.ui.components.TopBarComponent
import com.mtzdev.mywheatherapp.ui.screen.notification.NotificationScreen
import com.mtzdev.mywheatherapp.ui.screen.settings.SettingsScreen
import com.mtzdev.mywheatherapp.ui.screen.weatherdata.WeatherDataScreen

class HomeScreen: Screen {

    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<HomeScreenModel>()
        val state by screenModel.state.collectAsState()
        LaunchedEffect (Unit){
            screenModel.setEvent(HomeContract.Event.ChangeUbication("Bogotá"))
        }
        HandleEffects(screenModel)
        Scaffold(
            topBar = {
                val name = state.geoData.localNames?.es ?: state.geoData.name
                TopBarComponent(
                    title = name.orEmpty(),
                    loading = state.loadingGeo,
                    onLeftIconClick = {
                        screenModel.setEvent(HomeContract.Event.NavigateToNotification)
                    },
                    onRightIconClick = {
                        screenModel.setEvent(HomeContract.Event.NavigateToSettings)
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier.padding(paddingValues)
            ) {
                Navigator(WeatherDataScreen(state.geoData))
            }
        }
    }

    @Composable
    private fun HandleEffects(screenModel: HomeScreenModel){
        val navigator = LocalNavigator.current
        val effect = screenModel.effect.collectAsState(null)
        LaunchedEffect(effect.value) {
            when(effect.value){
                is HomeContract.Effect.ShowError -> {

                }
                HomeContract.Effect.NavigateToNotification -> navigator?.push(NotificationScreen())
                HomeContract.Effect.NavigateToSettings -> navigator?.push(SettingsScreen())
                null -> Unit
            }
        }

    }
}