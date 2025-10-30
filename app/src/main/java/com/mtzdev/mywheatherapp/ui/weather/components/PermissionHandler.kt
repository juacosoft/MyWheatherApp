package com.mtzdev.mywheatherapp.ui.weather.components

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.shouldShowRationale

/**
 * Composable that handles location permission requests for weather GPS feature.
 * Manages permission states and shows appropriate dialogs based on user actions.
 *
 * This component implements the complete permission flow:
 * - Initial permission request
 * - Rationale dialog when permissions are denied
 * - Settings navigation when permissions are permanently denied
 *
 * @param onPermissionGranted Callback invoked when all location permissions are granted
 * @param onPermissionDenied Callback invoked when permissions are denied by user
 * @param onPermissionPermanentlyDenied Callback invoked when permissions are permanently denied
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionHandler(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit,
    onPermissionPermanentlyDenied: () -> Unit
) {
    // T021: Core permission state management with Accompanist
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // State for controlling dialog visibility
    var showRationaleDialog by remember { mutableStateOf(false) }
    var showPermanentlyDeniedDialog by remember { mutableStateOf(false) }

    // T023: Permission state management with LaunchedEffect
    LaunchedEffect(permissionsState.allPermissionsGranted) {
        when {
            // All permissions granted - notify success
            permissionsState.allPermissionsGranted -> {
                onPermissionGranted()
            }
            // At least one permission should show rationale - show explanation dialog
            permissionsState.permissions.any { it.status.shouldShowRationale } -> {
                showRationaleDialog = true
            }
            // Permissions were requested but denied without rationale - permanently denied
            permissionsState.permissions.any {
                !it.status.isGranted && !it.status.shouldShowRationale
            } -> {
                // Only show permanently denied if we've already requested permissions
                if (permissionsState.permissions.any { !it.status.isGranted }) {
                    showPermanentlyDeniedDialog = true
                }
            }
        }
    }

    // T022: Permission rationale dialog
    if (showRationaleDialog) {
        RationaleDialog(
            onDismiss = {
                showRationaleDialog = false
                onPermissionDenied()
            },
            onConfirm = {
                showRationaleDialog = false
                permissionsState.launchMultiplePermissionRequest()
            }
        )
    }

    // T024: Permanently denied dialog
    if (showPermanentlyDeniedDialog) {
        PermanentlyDeniedDialog(
            onDismiss = {
                showPermanentlyDeniedDialog = false
                onPermissionDenied()
            },
            onOpenSettings = {
                showPermanentlyDeniedDialog = false
                onPermissionPermanentlyDenied()
            }
        )
    }
}

/**
 * Dialog that explains why location permissions are needed.
 * Shown when user denies permissions for the first time.
 *
 * @param onDismiss Callback when user cancels or closes the dialog
 * @param onConfirm Callback when user agrees to grant permissions
 */
@Composable
private fun RationaleDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Permisos de Ubicación Necesarios",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Text(
                text = "Esta aplicación necesita acceso a tu ubicación para mostrarte " +
                        "el clima actual de tu zona. Si no deseas compartir tu ubicación, " +
                        "puedes buscar ubicaciones manualmente en cualquier momento.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Permitir")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

/**
 * Dialog shown when location permissions are permanently denied.
 * Guides user to app settings to manually enable permissions.
 *
 * @param onDismiss Callback when user chooses manual search
 * @param onOpenSettings Callback when user chooses to open settings
 */
@Composable
private fun PermanentlyDeniedDialog(
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Permiso Requerido",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Text(
                text = "Los permisos de ubicación fueron denegados permanentemente. " +
                        "Para usar la detección automática de ubicación, debes habilitar " +
                        "los permisos en la configuración de la aplicación.\n\n" +
                        "Alternativamente, puedes usar la búsqueda manual de ubicaciones.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onOpenSettings()
                    // Open app settings using Intent
                    val intent = Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", context.packageName, null)
                    )
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                }
            ) {
                Text("Ir a Configuración")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Usar Búsqueda Manual")
            }
        }
    )
}

// Preview annotations for development and design verification

@Preview(showBackground = true)
@Composable
private fun RationaleDialogPreview() {
    MaterialTheme {
        RationaleDialog(
            onDismiss = {},
            onConfirm = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PermanentlyDeniedDialogPreview() {
    MaterialTheme {
        PermanentlyDeniedDialog(
            onDismiss = {},
            onOpenSettings = {}
        )
    }
}
