package com.mtzdev.mywheatherapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for system-level data (country, etc.).
 */
@Serializable
data class SysDto(
    @SerialName("country")
    val country: String
)
