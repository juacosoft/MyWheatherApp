package com.mtzdev.mywheatherapp.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction

/**
 * Componente de búsqueda de ubicación por nombre de ciudad.
 *
 * Input de texto con icono de búsqueda y loading state.
 *
 * @param searchQuery Texto actual del input
 * @param onSearchQueryChange Callback cuando cambia el texto
 * @param onSearchSubmit Callback cuando se presiona Enter o botón de búsqueda
 * @param isLoading Si está en proceso de búsqueda (muestra spinner)
 * @param enabled Si el input está habilitado para edición
 * @param modifier Modificador de Compose
 */
@Composable
fun LocationSearchInput(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchSubmit: () -> Unit,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled && !isLoading,
        label = { Text("Buscar ciudad") },
        placeholder = { Text("Ej: Bogotá, London, Tokyo...") },
        trailingIcon = {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                IconButton(
                    onClick = onSearchSubmit,
                    enabled = enabled && searchQuery.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar"
                    )
                }
            }
        },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                if (searchQuery.isNotBlank() && !isLoading) {
                    onSearchSubmit()
                }
            }
        ),
        singleLine = true
    )
}
