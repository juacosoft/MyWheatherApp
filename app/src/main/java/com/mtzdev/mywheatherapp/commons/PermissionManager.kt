package com.mtzdev.mywheatherapp.commons

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Gestor centralizado de permisos de ubicación.
 *
 * Esta clase proporciona utilidades para verificar y manejar permisos
 * de ubicación de manera consistente en toda la aplicación.
 *
 * @property context Contexto de la aplicación (usar Application context)
 */
class PermissionManager(private val context: Context) {

    companion object {
        /**
         * Permisos de ubicación requeridos por la app.
         */
        val LOCATION_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    /**
     * Verifica si la app tiene permisos de ubicación otorgados.
     *
     * @return true si tiene al menos uno de los permisos (FINE o COARSE), false en caso contrario
     */
    fun hasLocationPermission(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocation || coarseLocation
    }

    /**
     * Verifica si se debe mostrar rationale (explicación) antes de solicitar permiso.
     *
     * Retorna true si el usuario previamente denegó el permiso pero NO seleccionó
     * "No volver a preguntar". En este caso, se recomienda mostrar un diálogo
     * explicativo antes de volver a solicitar el permiso.
     *
     * @param activity Actividad desde donde se solicitará el permiso
     * @return true si se debe mostrar rationale, false en caso contrario
     */
    fun shouldShowRationale(activity: Activity): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) || ActivityCompat.shouldShowRequestPermissionRationale(
            activity,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    /**
     * Verifica si el permiso fue denegado permanentemente ("No volver a preguntar").
     *
     * Cuando el usuario selecciona "No volver a preguntar", shouldShowRationale retorna false
     * y el permiso no está otorgado. En este caso, la única opción es enviar al usuario
     * a la configuración de la app.
     *
     * @param activity Actividad para verificar
     * @return true si el permiso está permanentemente denegado, false en caso contrario
     */
    fun isPermissionPermanentlyDenied(activity: Activity): Boolean {
        return !hasLocationPermission() && !shouldShowRationale(activity)
    }

    /**
     * Crea un Intent para abrir la configuración de permisos de la app.
     *
     * Útil cuando el usuario denegó permanentemente el permiso y necesita
     * habilitarlo manualmente desde Configuración.
     *
     * @return Intent para abrir configuración de la app
     */
    fun createAppSettingsIntent(): Intent {
        return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }
}
