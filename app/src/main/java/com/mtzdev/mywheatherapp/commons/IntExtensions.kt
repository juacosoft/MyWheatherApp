package com.mtzdev.mywheatherapp.commons

fun Int.formatToVisibility(): String{
    return if (this >= 1000) {
        "${this / 1000}km"
    } else {
        "${this}km"
    }
}