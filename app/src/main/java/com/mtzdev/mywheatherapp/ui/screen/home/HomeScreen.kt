package com.mtzdev.mywheatherapp.ui.screen.home

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import com.mtzdev.mywheatherapp.ui.components.TopBarComponent
import com.mtzdev.mywheatherapp.ui.screen.hourly.WeatherDataHourlyScreen
import com.mtzdev.mywheatherapp.ui.screen.notification.NotificationScreen
import com.mtzdev.mywheatherapp.ui.screen.settings.SettingsScreen
import com.mtzdev.mywheatherapp.ui.weather.navigation.WeatherTab

class HomeScreen: Screen {

    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<HomeScreenModel>()
        val state by screenModel.state.collectAsState()
        var paddingState by remember { mutableStateOf<PaddingValues>(PaddingValues(4.dp)) }
        LaunchedEffect (Unit){
            screenModel.setEvent(HomeContract.Event.ChangeUbication("Bogotá"))
        }
        HandleEffects(screenModel)
        if (!state.loadingGeo){
            TabNavigator(WeatherTab.create(paddingState)){
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
                    },
                    bottomBar = {
                        NavigationBar {
                            TabNavigationItem(WeatherTab.create(paddingState))
                            TabNavigationItem(WeatherDataHourlyScreen(paddingState))
                        }
                    }
                ) { paddingValues ->
                    paddingState = paddingValues
                    CurrentTab()
                }
            }
        }
    }

    @Composable
    private fun RowScope.TabNavigationItem(tab: Tab) {
        val tabNavigator = LocalTabNavigator.current
        val defaultIcon = rememberVectorPainter(Icons.Default.Warning)
        NavigationBarItem(
            selected = tabNavigator.current == tab,
            onClick = { tabNavigator.current = tab },
            icon = { Icon(painter = tab.options.icon?:defaultIcon, contentDescription = tab.options.title) }
        )
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