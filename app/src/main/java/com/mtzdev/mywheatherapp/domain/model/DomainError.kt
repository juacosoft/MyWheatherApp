package com.mtzdev.mywheatherapp.domain.model

/**
 * Sealed hierarchy of domain-level errors.
 * These represent business logic errors, not infrastructure exceptions.
 * All error types should be handled gracefully in the presentation layer.
 */
sealed class DomainError {

    /**
     * Location-related errors.
     * Covers GPS, permissions, and location service issues.
     */
    sealed class LocationError : DomainError() {
        /**
         * User denied location permissions.
         * Recovery: Request permissions or use manual search.
         */
        data object PermissionDenied : LocationError()

        /**
         * Location is unavailable (service disabled, no signal, etc.).
         * Recovery: Enable GPS or use manual search.
         */
        data object Unavailable : LocationError()

        /**
         * Location request timed out (>10 seconds).
         * Recovery: Retry or use manual search.
         */
        data object Timeout : LocationError()

        /**
         * GPS is disabled in device settings.
         * Recovery: Enable GPS in settings or use manual search.
         */
        data object GpsDisabled : LocationError()
    }

    /**
     * Weather API-related errors.
     * Covers network, server, and data retrieval issues.
     */
    sealed class WeatherError : DomainError() {
        /**
         * Requested city was not found in geocoding API.
         * Recovery: Verify city name and try again.
         */
        data object CityNotFound : WeatherError()

        /**
         * API key is invalid or missing.
         * Recovery: Check API key configuration.
         */
        data object InvalidApiKey : WeatherError()

        /**
         * API rate limit exceeded (too many requests).
         * Recovery: Wait before retrying.
         */
        data object RateLimitExceeded : WeatherError()

        /**
         * No internet connection available.
         * Recovery: Check internet connection and retry.
         */
        data object NoInternetConnection : WeatherError()

        /**
         * Server returned 5xx error.
         * Recovery: Wait and retry later.
         */
        data object ServerError : WeatherError()

        /**
         * Unknown or unexpected error occurred.
         *
         * @property message Error message if available
         */
        data class Unknown(val message: String?) : WeatherError()
    }

    /**
     * Validation errors for user input or data.
     * Covers invalid coordinates, empty fields, etc.
     */
    sealed class ValidationError : DomainError() {
        /**
         * City name is empty or blank.
         * Recovery: Prompt user to enter city name.
         */
        data object EmptyCityName : ValidationError()

        /**
         * Coordinates are out of valid range.
         * Recovery: Use valid coordinates or manual search.
         */
        data object InvalidCoordinates : ValidationError()
    }
}
