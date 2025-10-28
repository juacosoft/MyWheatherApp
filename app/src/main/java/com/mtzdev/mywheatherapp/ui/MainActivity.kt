package com.mtzdev.mywheatherapp.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cafe.adriel.voyager.navigator.Navigator
import com.mtzdev.mywheatherapp.ui.screen.home.HomeScreen
import com.mtzdev.mywheatherapp.ui.theme.MyWheatherAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyWheatherAppTheme {
                Navigator(HomeScreen())
            }
        }
    }
}