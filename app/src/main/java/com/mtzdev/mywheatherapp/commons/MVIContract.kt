package com.mtzdev.mywheatherapp.commons

/**
 * Contrato base para la implementación de MVI.
 * Define las interfaces marcadoras para los componentes de MVI.
 */
interface MVIContract {
    /** Interfaz marcadora para los estados de la UI (UiState). */
    interface UiState

    /** Interfaz marcadora para los eventos de la UI (UiEvent/Intent). */
    interface UiEvent

    /** Interfaz marcadora para los efectos secundarios (Side Effects). */
    interface Effect
}
