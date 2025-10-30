package com.mtzdev.mywheatherapp.domain.model

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Domain model representing a geographic location.
 * Contains coordinates and location metadata for weather queries.
 *
 * @property latitude Latitude coordinate in decimal degrees (-90.0 to 90.0)
 * @property longitude Longitude coordinate in decimal degrees (-180.0 to 180.0)
 * @property name City or location name
 * @property country ISO 3166-1 alpha-2 country code (2 letters)
 * @throws IllegalArgumentException if coordinates are out of valid range
 */
data class Location(
    val latitude: Double,
    val longitude: Double,
    val name: String,
    val country: String
) {
    init {
        require(latitude in -90.0..90.0) {
            "Latitude must be between -90.0 and 90.0, got: $latitude"
        }
        require(longitude in -180.0..180.0) {
            "Longitude must be between -180.0 and 180.0, got: $longitude"
        }
        require(name.isNotBlank()) {
            "Location name cannot be blank"
        }
        require(country.length == 2) {
            "Country code must be 2 characters (ISO 3166-1 alpha-2), got: $country"
        }
    }

    /**
     * Returns a human-readable location string.
     * Format: "CityName, CountryCode"
     * Example: "Madrid, ES"
     */
    fun displayName(): String = "$name, $country"

    /**
     * Calculates distance to another location in kilometers.
     * Uses Haversine formula for great-circle distance.
     *
     * @param other Target location
     * @return Distance in kilometers
     */
    fun distanceTo(other: Location): Double {
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(other.latitude - latitude)
        val dLon = Math.toRadians(other.longitude - longitude)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(latitude)) *
                cos(Math.toRadians(other.latitude)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadiusKm * c
    }
}
