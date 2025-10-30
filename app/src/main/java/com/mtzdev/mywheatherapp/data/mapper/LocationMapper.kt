package com.mtzdev.mywheatherapp.data.mapper

import com.mtzdev.mywheatherapp.data.remote.dto.GeocodingResponseDto
import com.mtzdev.mywheatherapp.domain.model.Location

/**
 * T056: Mapper for converting GeocodingResponseDto to Location domain model.
 *
 * Maps:
 * - lat/lon → latitude/longitude
 * - name → name
 * - country → country
 * - Handles optional state field (e.g., "Madrid, Community of Madrid")
 */
class LocationMapper {

    /**
     * Convert GeocodingResponseDto to Location domain model.
     *
     * @param dto The DTO from the Geocoding API
     * @return Location domain model
     * @throws IllegalArgumentException if coordinates are invalid
     */
    fun mapToDomain(dto: GeocodingResponseDto): Location {
        // Build location name with state if available
        val locationName = buildLocationName(dto.name, dto.state)

        return Location(
            latitude = dto.lat,
            longitude = dto.lon,
            name = locationName,
            country = dto.country
        )
    }

    /**
     * Build location name combining city and state.
     *
     * Examples:
     * - "Madrid" + "Community of Madrid" → "Madrid, Community of Madrid"
     * - "Madrid" + null → "Madrid"
     */
    private fun buildLocationName(name: String, state: String?): String {
        return if (state != null && state.isNotBlank()) {
            "$name, $state"
        } else {
            name
        }
    }
}
