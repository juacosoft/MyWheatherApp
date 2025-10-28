package com.mtzdev.mywheatherapp.ui.screen.weatherdata

import androidx.compose.ui.graphics.Color
import com.mtzdev.mywheatherapp.R

enum class WeatherColors(
    val bg: List<Color>,
    val colorText: Color,
    val image: Int
    ) {
        SUNNY(listOf(
            Color(0xFFFFC107),
            Color(0xFFFF9800)
        ), Color.White, R.drawable.img_sunny),
        CLOUDY(listOf(
            Color(0xFF33A2E8),
            Color(0xFF236B9C)
        ), Color.White, R.drawable.img_clouds),
        NO_DATA(listOf(
            Color(0xFF607D8B),
            Color(0xFF455A64)
        ), Color.White, R.drawable.ic_cloud_off)
    }