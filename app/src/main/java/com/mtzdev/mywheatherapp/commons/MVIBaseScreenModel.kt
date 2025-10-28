package com.mtzdev.mywheatherapp.commons

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

abstract class MVIBaseScreenMode<
        UiState : MVIContract.UiState,
        UiEvent : MVIContract.UiEvent,
        Effects : MVIContract.Effect
        >(initialState: UiState) : StateScreenModel<UiState>(initialState) {

    private val _effect: MutableSharedFlow<Effects> = MutableSharedFlow()
    val effect: SharedFlow<Effects> = _effect.asSharedFlow()

    fun setEvent(event: UiEvent) {
        handleEvent(event)
    }

    protected abstract fun handleEvent(event: UiEvent)

    protected fun sendEffect(builder:() -> Effects) {
        val effectValue = builder()
        screenModelScope.launch {
            _effect.emit(effectValue)
        }
    }
}