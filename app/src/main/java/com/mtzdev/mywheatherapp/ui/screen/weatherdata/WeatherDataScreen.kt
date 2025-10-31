package com.mtzdev.mywheatherapp.ui.screen.weatherdata

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.mtzdev.mywheatherapp.commons.formatToVisibility
import com.mtzdev.mywheatherapp.domain.entity.WeatherGeoDataEntity
import com.mtzdev.mywheatherapp.ui.components.LoadingComponent

class WeatherDataScreen(
    private val weatherGeoDataEntity: WeatherGeoDataEntity,
    private val modifier: Modifier = Modifier
): Tab {

    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<WeatherDataScreenModel>()
        val state by screenModel.state.collectAsState()
        LaunchedEffect(Unit) {
            screenModel.setEvent(WeatherDataContract.Event.Init(weatherGeoDataEntity))
        }
        Surface(modifier = modifier
            .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .padding(18.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = state.colors.bg
                        ),
                        shape = MaterialTheme.shapes.extraLarge
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (state.loadingWeather){
                    LoadingComponent(text = "Getting weather info")
                } else {
                    val weatherColors = state.colors
                    Column(
                        modifier = Modifier.padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(weatherColors.image),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 30.dp),
                            contentDescription = null
                        )
                        Text(
                            "${state.weatherData?.main?.temp}°C", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = weatherColors.colorText,
                        )
                        val city = state.currentGeoData?.localNames?.es ?: state.currentGeoData?.name
                        Text(
                            city?:"No data", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = weatherColors.colorText,
                        )
                        Spacer(Modifier.height(50.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "Wind speed",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = weatherColors.colorText.copy(alpha = 0.3f)
                                )
                                Text("${state.weatherData?.wind?.speed}/mps", style = MaterialTheme.typography.titleLarge, color = weatherColors.colorText)
                            }
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "Humidity",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = weatherColors.colorText.copy(alpha = 0.3f)
                                )
                                Text("${state.weatherData?.main?.humidity}%", style = MaterialTheme.typography.titleLarge, color = weatherColors.colorText)
                            }
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "Visibility",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = weatherColors.colorText.copy(alpha = 0.3f)
                                )
                                Text(state.weatherData?.visibility?.formatToVisibility()?:"0", style = MaterialTheme.typography.titleLarge, color = weatherColors.colorText)
                            }
                        }
                    }
                }
            }
        }
    }

    override val options: TabOptions
        @Composable
        get() {
            val title = "Home"
            val icon = rememberVectorPainter(Icons.Default.Home)
            return remember {
                TabOptions(
                    index = 0u,
                    title = title,
                    icon = icon
                )
            }
        }
}