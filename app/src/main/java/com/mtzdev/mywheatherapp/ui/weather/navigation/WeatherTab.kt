package com.mtzdev.mywheatherapp.ui.weather.navigation

import androidx.compose.ui.Modifier
import com.mtzdev.mywheatherapp.ui.weather.WeatherScreen

/**
 * T096: WeatherTab - Navigation alias for WeatherScreen.
 *
 * Provides a convenient way to access the weather tab from navigation.
 * WeatherScreen already implements the Tab interface with all necessary
 * configuration (icon, title, content).
 */
object WeatherTab {
    /**
     * Creates an instance of WeatherScreen configured as a Tab.
     *
     * @return WeatherScreen instance ready for TabNavigator
     */
    fun create(modifier: Modifier) = WeatherScreen(modifier)
}
